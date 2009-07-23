//
// MIASReader.java
//

/*
OME Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.formats.in;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Vector;

import loci.common.ByteArrayHandle;
import loci.common.Location;
import loci.common.RandomAccessInputStream;
import loci.common.Region;
import loci.formats.CoreMetadata;
import loci.formats.FilePattern;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.MetadataTools;
import loci.formats.meta.FilterMetadata;
import loci.formats.meta.MetadataStore;
import loci.formats.tiff.IFD;
import loci.formats.tiff.TiffParser;

/**
 * MIASReader is the file format reader for Maia Scientific MIAS-2 datasets.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/in/MIASReader.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/in/MIASReader.java">SVN</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
public class MIASReader extends FormatReader {

  // -- Fields --

  /** TIFF files - indexed by plate, well, and file. */
  private String[][][] tiffs;

  /** Delegate readers. */
  private MinimalTiffReader[][][] readers;

  /** Path to file containing analysis results for all plates. */
  private String resultFile = null;

  /** Other files that contain analysis results. */
  private Vector<String> analysisFiles = null;

  private int[][] wellNumber;
  private int[][] plateAndWell;

  private int tileRows, tileCols;
  private int tileWidth, tileHeight;

  private int wellRows, wellColumns;
  private int[] bpp;

  private Vector<String> plateDirs;

  // -- Constructor --

  /** Constructs a new MIAS reader. */
  public MIASReader() {
    super("MIAS", new String[] {"tif", "tiff"});
    suffixSufficient = false;
    blockCheckLen = 1048576;
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#setSeries(int) */
  public void setSeries(int series) {
    int oldSeries = getSeries();
    super.setSeries(series);
    try {
      unmapSeries(getPlateNumber(oldSeries), getWellNumber(oldSeries));
    }
    catch (IOException e) {
      traceDebug(e);
    }
  }

  /* @see loci.formats.IFormatReader#isThisType(String, boolean) */
  public boolean isThisType(String filename, boolean open) {
    Location baseFile = new Location(filename).getAbsoluteFile();
    Location wellDir = baseFile.getParentFile();
    Location experiment = wellDir.getParentFile().getParentFile();

    return wellDir != null && experiment != null &&
      wellDir.getName().startsWith("Well") && super.isThisType(filename, open);
  }

  /* @see loci.formats.IFormatReader#isThisType(RandomAccessInputStream) */
  public boolean isThisType(RandomAccessInputStream stream) throws IOException {
    if (!FormatTools.validStream(stream, blockCheckLen, false)) return false;

    TiffParser tp = new TiffParser(stream);
    IFD ifd = tp.getFirstIFD();
    if (ifd == null) return false;

    Object s = ifd.getIFDValue(IFD.SOFTWARE);
    if (s == null) return false;
    String software = null;
    if (s instanceof String[]) software = ((String[]) s)[0];
    else software = s.toString();

    return software.startsWith("eaZYX");
  }

  /* @see loci.formats.IFormatReader#fileGroupOption(String) */
  public int fileGroupOption(String id) throws FormatException, IOException {
    return FormatTools.MUST_GROUP;
  }

  /* @see loci.formats.IFormatReader#get8BitLookupTable() */
  public byte[][] get8BitLookupTable() throws FormatException, IOException {
    FormatTools.assertId(currentId, true, 1);
    return readers == null ? null : readers[0][0][0].get8BitLookupTable();
  }

  /* @see loci.formats.IFormatReader#get16BitLookupTable() */
  public short[][] get16BitLookupTable() throws FormatException, IOException {
    FormatTools.assertId(currentId, true, 1);
    return readers == null ? null : readers[0][0][0].get16BitLookupTable();
  }

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.checkPlaneParameters(this, no, buf.length, x, y, w, h);

    int well = getWellNumber(getSeries());
    int plate = getPlateNumber(getSeries());

    if (Location.getMappedFile(getFilename(plate, well, 0)) == null) {
      preReadSeries(plate, well);
    }

    if (tileRows == 1 && tileCols == 1) {
      readers[plate][well][no].setId(tiffs[plate][well][no]);
      readers[plate][well][no].openBytes(0, buf, x, y, w, h);
      return buf;
    }

    int outputRowLen = w * bpp[getSeries()];

    Region image = new Region(x, y, w, h);
    int outputRow = 0, outputCol = 0;
    Region intersection = null;

    byte[] tileBuf = null;

    for (int row=0; row<tileRows; row++) {
      for (int col=0; col<tileCols; col++) {
        Region tile =
          new Region(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
        if (!tile.intersects(image)) continue;

        intersection = tile.intersection(image);

        int tileIndex = (no * tileRows + row) * tileCols + col;
        tileBuf = getTile(plate, well, no, row, col, intersection);

        int rowLen = tileBuf.length / intersection.height;

        // copy tile into output image
        int outputOffset = outputRow * outputRowLen + outputCol;
        for (int trow=0; trow<intersection.height; trow++) {
          System.arraycopy(tileBuf, trow * rowLen, buf, outputOffset, rowLen);
          outputOffset += outputRowLen;
        }

        outputCol += rowLen;
      }
      if (intersection != null) {
        outputRow += intersection.height;
        outputCol = 0;
      }
    }

    return buf;
  }

  /* @see loci.formats.IFormatReader#getUsedFiles(boolean) */
  public String[] getUsedFiles(boolean noPixels) {
    FormatTools.assertId(currentId, true, 1);
    Vector<String> files = new Vector<String>();
    if (!noPixels) {
      for (String[][] plates : tiffs) {
        for (String[] wells : plates) {
          for (String file : wells) {
            files.add(file);
          }
        }
      }
    }
    if (resultFile != null) files.add(resultFile);
    for (String file : analysisFiles) {
      files.add(file);
    }
    return files.toArray(new String[0]);
  }

  // -- IFormatHandler API methods --

  /* @see loci.formats.IFormatHandler#close(boolean) */
  public void close(boolean fileOnly) throws IOException {
    super.close(fileOnly);
    if (readers != null) {
      for (MinimalTiffReader[][] wells : readers) {
        for (MinimalTiffReader[] images : wells) {
          for (MinimalTiffReader r : images) {
            if (r != null) r.close(fileOnly);
          }
        }
      }
    }
    if (!fileOnly) {
      readers = null;
      tiffs = null;
      tileRows = tileCols = 0;
      resultFile = null;
      analysisFiles = null;
      wellNumber = null;
      tileWidth = tileHeight = 0;
      plateDirs = null;
      wellRows = wellColumns = 0;
      bpp = null;
      plateAndWell = null;
    }
  }

  // -- Internal FormatReader API methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  protected void initFile(String id) throws FormatException, IOException {
    debug("MIASReader.initFile(" + id + ")");
    super.initFile(id);

    // TODO : initFile currently accepts a constituent TIFF file.
    // Consider allowing the top level experiment directory to be passed in

    analysisFiles = new Vector<String>();
    plateDirs = new Vector<String>();

    // MIAS is a high content screening format which supports multiple plates,
    // wells and fields.
    // Most of the metadata comes from the directory hierarchy, as very little
    // metadata is present in the actual files.
    //
    // The directory hierarchy is:
    //
    // <experiment name>                        top level experiment directory
    //   Batchresults                           analysis results for experiment
    //   <plate number>_<plate barcode>         one directory for each plate
    //     results                              analysis results for plate
    //     Well<xxxx>                           one directory for each well
    //       mode<x>_z<xxx>_t<xxx>_im<x>_<x>.tif
    //
    // Each TIFF file contains a single grayscale plane.  The "mode" block
    // refers to the channel number; the "z" and "t" blocks refer to the
    // Z section and timepoint, respectively.  The "im<x>_<x>" block gives
    // the row and column coordinates of the image within a mosaic.
    //
    // We are initially given one of these TIFF files; from there, we need
    // to find the top level experiment directory and work our way down to
    // determine how many plates and wells are present.

    status("Building list of TIFF files");

    Location baseFile = new Location(id).getAbsoluteFile();
    Location experiment =
      baseFile.getParentFile().getParentFile().getParentFile();

    String experimentName = experiment.getName();

    String[] directories = experiment.list(true);
    Arrays.sort(directories);
    for (String dir : directories) {
      if (dir.equals("Batchresults")) {
        Location f = new Location(experiment, dir);
        String[] results = f.list(true);
        for (String result : results) {
          Location file = new Location(f, result);
          analysisFiles.add(file.getAbsolutePath());
          if (result.startsWith("NEO_Results")) {
            resultFile = file.getAbsolutePath();
          }
        }
      }
      else plateDirs.add(dir);
    }

    int nPlates = plateDirs.size();

    tiffs = new String[nPlates][][];
    readers = new MinimalTiffReader[nPlates][][];

    int[][] zCount = new int[nPlates][];
    int[][] cCount = new int[nPlates][];
    int[][] tCount = new int[nPlates][];
    String[][] order = new String[nPlates][];
    wellNumber = new int[nPlates][];

    for (int i=0; i<nPlates; i++) {
      String plate = plateDirs.get(i);
      Location plateDir = new Location(experiment, plate);
      String[] list = plateDir.list(true);
      Arrays.sort(list);
      Vector<String> wellDirectories = new Vector<String>();
      for (String dir : list) {
        Location f = new Location(plateDir, dir);
        if (f.getName().startsWith("Well")) {
          wellDirectories.add(f.getAbsolutePath());
        }
        else if (f.getName().equals("results")) {
          String[] resultsList = f.list(true);
          for (String result : resultsList) {
            // exclude proprietary program state files
            if (!result.endsWith(".sav") && !result.endsWith(".dsv")) {
              Location r = new Location(f, result);
              analysisFiles.add(r.getAbsolutePath());
            }
          }
        }
      }
      readers[i] = new MinimalTiffReader[wellDirectories.size()][];
      tiffs[i] = new String[wellDirectories.size()][];
      zCount[i] = new int[wellDirectories.size()];
      cCount[i] = new int[wellDirectories.size()];
      tCount[i] = new int[wellDirectories.size()];
      order[i] = new String[wellDirectories.size()];
      wellNumber[i] = new int[wellDirectories.size()];

      for (int j=0; j<wellDirectories.size(); j++) {
        Location well = new Location(wellDirectories.get(j));

        String wellName = well.getName().replaceAll("Well", "");
        wellNumber[i][j] = Integer.parseInt(wellName) - 1;

        String wellPath = well.getAbsolutePath();
        String[] tiffFiles = well.list(true);
        Vector<String> tmpFiles = new Vector<String>();
        for (String tiff : tiffFiles) {
          String name = tiff.toLowerCase();
          if (name.endsWith(".tif") || name.endsWith(".tiff")) {
            tmpFiles.add(tiff);
          }
        }
        tiffFiles = tmpFiles.toArray(new String[0]);

        FilePattern fp = new FilePattern(tiffFiles[0], wellPath);
        String[] blocks = fp.getPrefixes();
        BigInteger[] firstNumber = fp.getFirst();
        BigInteger[] lastNumber = fp.getLast();
        BigInteger[] step = fp.getStep();

        order[i][j] = "XY";

        for (int block=blocks.length - 1; block>=0; block--) {
          blocks[block] = blocks[block].toLowerCase();
          blocks[block] =
            blocks[block].substring(blocks[block].lastIndexOf("_") + 1);

          BigInteger tmp = lastNumber[block].subtract(firstNumber[block]);
          tmp = tmp.add(BigInteger.ONE).divide(step[block]);
          int count = tmp.intValue();

          if (blocks[block].equals("z")) {
            zCount[i][j] = count;
            order[i][j] += "Z";
          }
          else if (blocks[block].equals("t")) {
            tCount[i][j] = count;
            order[i][j] += "T";
          }
          else if (blocks[block].equals("mode")) {
            cCount[i][j] = count;
            order[i][j] += "C";
          }
          else if (blocks[block].equals("im")) tileRows = count;
          else if (blocks[block].equals("")) tileCols = count;
          else {
            throw new FormatException("Unsupported block '" + blocks[block]);
          }
        }

        Arrays.sort(tiffFiles);
        tiffs[i][j] = new String[tiffFiles.length];
        readers[i][j] = new MinimalTiffReader[tiffFiles.length];
        for (int k=0; k<tiffFiles.length; k++) {
          tiffs[i][j][k] =
            new Location(wellPath, tiffFiles[k]).getAbsolutePath();
          readers[i][j][k] = new MinimalTiffReader();
        }
      }
    }

    // Populate core metadata

    status("Populating core metadata");

    int nSeries = 0;
    for (int i=0; i<tiffs.length; i++) {
      nSeries += tiffs[i].length;
    }

    core = new CoreMetadata[nSeries];
    bpp = new int[nSeries];
    plateAndWell = new int[nSeries][2];

    // assume that all wells have the same dimensions
    readers[0][0][0].setId(tiffs[0][0][0]);
    tileWidth = readers[0][0][0].getSizeX();
    tileHeight = readers[0][0][0].getSizeY();

    for (int i=0; i<core.length; i++) {
      Arrays.fill(plateAndWell[i], -1);
      core[i] = new CoreMetadata();

      int plate = getPlateNumber(i);
      int well = getWellNumber(i);

      core[i].sizeZ = zCount[plate][well];
      core[i].sizeC = cCount[plate][well];
      core[i].sizeT = tCount[plate][well];

      if (core[i].sizeZ == 0) core[i].sizeZ = 1;
      if (core[i].sizeC == 0) core[i].sizeC = 1;
      if (core[i].sizeT == 0) core[i].sizeT = 1;

      core[i].sizeX = tileWidth * tileCols;
      core[i].sizeY = tileHeight * tileRows;
      core[i].pixelType = readers[0][0][0].getPixelType();
      core[i].sizeC *= readers[0][0][0].getSizeC();
      core[i].rgb = readers[0][0][0].isRGB();
      core[i].littleEndian = readers[0][0][0].isLittleEndian();
      core[i].interleaved = readers[0][0][0].isInterleaved();
      core[i].indexed = readers[0][0][0].isIndexed();
      core[i].falseColor = readers[0][0][0].isFalseColor();
      core[i].dimensionOrder = order[plate][well];

      if (core[i].dimensionOrder.indexOf("Z") == -1) {
        core[i].dimensionOrder += "Z";
      }
      if (core[i].dimensionOrder.indexOf("C") == -1) {
        core[i].dimensionOrder += "C";
      }
      if (core[i].dimensionOrder.indexOf("T") == -1) {
        core[i].dimensionOrder += "T";
      }

      core[i].imageCount = core[i].sizeZ * core[i].sizeT * cCount[plate][well];
      bpp[i] = FormatTools.getBytesPerPixel(core[i].pixelType);
    }

    // Populate metadata hashtable

    status("Populating metadata hashtable");

    wellColumns = 1;

    if (resultFile != null) {
      RandomAccessInputStream s = new RandomAccessInputStream(resultFile);

      String[] cols = null;
      Vector<String> rows = new Vector<String>();

      boolean doKeyValue = true;
      int nStarLines = 0;

      String analysisResults = s.readString((int) s.length());
      s.close();
      String[] lines = analysisResults.split("\n");

      for (String line : lines) {
        line = line.trim();
        if (line.length() == 0) continue;
        if (line.startsWith("******") && line.endsWith("******")) nStarLines++;

        if (doKeyValue) {
          String[] n = line.split("\t");
          if (n[0].endsWith(":")) n[0] = n[0].substring(0, n[0].length() - 1);
          if (n.length >= 2) addGlobalMeta(n[0], n[1]);
        }
        else {
          if (cols == null) cols = line.split("\t");
          else rows.add(line);
        }
        if (nStarLines == 2) doKeyValue = false;
      }

      for (String row : rows) {
        String[] d = row.split("\t");
        for (int col=3; col<cols.length; col++) {
          addGlobalMeta("Plate " + d[0] + ", Well " + d[2] + " " + cols[col],
            d[col]);

          if (cols[col].equals("AreaCode")) {
            String wellID = d[col].replaceAll("\\D", "");
            wellColumns = Integer.parseInt(wellID);
            wellRows = (int) (d[col].charAt(0) - 'A') + 1;
          }
        }
      }
    }

    // Populate MetadataStore

    status("Populating MetadataStore");

    MetadataStore store =
      new FilterMetadata(getMetadataStore(), isMetadataFiltered());
    MetadataTools.populatePixels(store, this);

    store.setExperimentID("Experiment:" + experimentName, 0);
    store.setExperimentType("Other", 0);

    // populate SPW metadata
    for (int plate=0; plate<tiffs.length; plate++) {
      store.setPlateColumnNamingConvention("1", plate);
      store.setPlateRowNamingConvention("A", plate);

      String plateDir = plateDirs.get(plate);
      plateDir = plateDir.substring(plateDir.indexOf("-") + 1);
      store.setPlateName(plateDir, plate);
      store.setPlateExternalIdentifier(plateDir, plate);

      for (int row=0; row<wellRows; row++) {
        for (int col=0; col<wellColumns; col++) {
          store.setWellRow(new Integer(row), plate, row * wellColumns + col);
          store.setWellColumn(new Integer(col), plate, row * wellColumns + col);
        }
      }

      for (int well=0; well<tiffs[plate].length; well++) {
        int wellIndex = wellNumber[plate][well];

        int series = getSeriesNumber(plate, well);
        store.setWellSampleImageRef("Image:" + series, plate, wellIndex, 0);
        store.setWellSampleIndex(new Integer(series), plate, wellIndex, 0);

        // populate Image/Pixels metadata
        store.setImageExperimentRef("Experiment:" + experimentName, series);
        char wellRow = (char) ('A' + (wellIndex / wellColumns));
        int wellCol = (well % wellColumns) + 1;

        store.setImageID("Image:" + series, series);
        store.setImageName("Plate #" + plate + ", Well " + wellRow + wellCol,
          series);
        MetadataTools.setDefaultCreationDate(store, id, series);
      }
    }

    // populate image-level ROIs
    String[] colors = new String[getSizeC()];
    for (String file : analysisFiles) {
      String name = new Location(file).getName();
      if (name.startsWith("Well") && name.endsWith("detail.txt")) {
        int[] position = getPositionFromFile(file);

        int series = getSeriesNumber(position[0], position[1]);

        RandomAccessInputStream s = new RandomAccessInputStream(file);
        String data = s.readString((int) s.length());
        String[] lines = data.split("\n");
        int start = 0;
        while (start < lines.length && !lines[start].startsWith("Label")) {
          start++;
        }
        if (start >= lines.length) continue;

        String[] columns = lines[start].split("\t");
        Vector<String> columnNames = new Vector<String>();
        for (String v : columns) columnNames.add(v);

        for (int i=start+1; i<lines.length; i++) {
          populateROI(columnNames, lines[i].split("\t"), series, i - start - 1,
            position[2], position[3], store);
        }

        s.close();
      }
      else if (name.startsWith("Well") && name.endsWith("AllModesOverlay.tif"))
      {
        // original color for each channel is stored in
        // <plate>/results/Well<nnnn>_mode<n>_z<nnn>_t<nnn>_AllModesOverlay.tif
        int[] position = getPositionFromFile(file);
        if (colors[position[4]] != null) continue;
        try {
          colors[position[4]] = getChannelColorFromFile(file);
        }
        catch (IOException e) { }
        if (colors[position[4]] == null) continue;

        int seriesIndex = getSeriesNumber(position[0], position[1]);
        for (int s=0; s<getSeriesCount(); s++) {
          store.setChannelComponentColorDomain(
            colors[position[4]], s, position[4], 0);
        }
      }
    }
  }

  // -- Helper methods --

  /**
   * Get the color associated with the given file's channel.
   * The file must be one of the
   * Well<nnnn>_mode<n>_z<nnn>_t<nnn>_AllModesOverlay.tif
   * files in <experiment>/<plate>/results/
   */
  private String getChannelColorFromFile(String file)
    throws FormatException, IOException
  {
    RandomAccessInputStream s = new RandomAccessInputStream(file);
    TiffParser tp = new TiffParser(s);
    IFD ifd = tp.getFirstIFD();
    s.close();
    int[] colorMap = ifd.getIFDIntArray(IFD.COLOR_MAP, false);
    if (colorMap == null) return null;

    int[] position = getPositionFromFile(file);

    int nEntries = colorMap.length / 3;
    int max = Integer.MIN_VALUE;
    int maxIndex = -1;
    for (int c=0; c<3; c++) {
      int v = (colorMap[c * nEntries] >> 8) & 0xff;
      if (v > max) {
        max = v;
        maxIndex = c;
      }
      else if (v == max) {
        return "gray";
      }
    }

    switch (maxIndex) {
      case 0:
        return "R";
      case 1:
        return "G";
      case 2:
        return "B";
    }
    return null;
  }

  /**
   * Returns an array of length 5 that contains the plate, well, time point,
   * Z and channel indices corresponding to the given analysis file.
   */
  private int[] getPositionFromFile(String file) {
    int[] position = new int[5];

    Location plate = new Location(file).getParentFile().getParentFile();
    if (file.endsWith(".tif")) plate = plate.getParentFile();
    String plateName = plate.getName();

    position[0] = plateDirs.indexOf(plateName);

    file = file.substring(file.lastIndexOf(File.separator) + 1);
    String wellIndex = file.substring(4, file.indexOf("_"));
    position[1] = Integer.parseInt(wellIndex) - 1;

    int tIndex = file.indexOf("_t") + 2;
    String t = file.substring(tIndex, file.indexOf("_", tIndex));
    position[2] = Integer.parseInt(t);

    int zIndex = file.indexOf("_z") + 2;
    String zValue = file.substring(zIndex, file.indexOf("_", zIndex));
    position[3] = Integer.parseInt(zValue);

    int cIndex = file.indexOf("mode") + 4;
    String cValue = file.substring(cIndex, file.indexOf("_", cIndex));
    position[4] = Integer.parseInt(cValue) - 1;

    return position;
  }

  private void populateROI(Vector<String> columns, String[] data, int series,
    int roi, int time, int z, MetadataStore store)
  {
    float cx = Float.parseFloat(data[columns.indexOf("Col")]);
    float cy = Float.parseFloat(data[columns.indexOf("Row")]);

    Integer tv = new Integer(time);
    Integer zv = new Integer(z);

    store.setROIT0(tv, series, roi);
    store.setROIT1(tv, series, roi);
    store.setROIZ0(zv, series, roi);
    store.setROIZ1(zv, series, roi);

    store.setShapeText(data[columns.indexOf("Label")], series, roi, 0);
    store.setShapeTheT(tv, series, roi, 0);
    store.setShapeTheZ(zv, series, roi, 0);
    store.setCircleCx(data[columns.indexOf("Col")], series, roi, 0);
    store.setCircleCy(data[columns.indexOf("Row")], series, roi, 0);

    float diam = Float.parseFloat(data[columns.indexOf("Cell Diam.")]);
    String radius = String.valueOf(diam / 2);

    store.setCircleR(radius, series, roi, 0);

    // NB: other attributes are "Nucleus Area", "Cell Type", and
    // "Mean Nucleus Intens."
  }

  /** Retrieve the series corresponding to the given plate and well. */
  private int getSeriesNumber(int plate, int well) {
    int series = 0;
    for (int i=0; i<plate; i++) {
      series += tiffs[i].length;
    }
    return series + well;
  }

  /** Retrieve the well to which this series belongs. */
  private int getWellNumber(int series) {
    if (plateAndWell[series][1] == -1) {
      plateAndWell[series] = getPlateAndWell(series);
    }
    return plateAndWell[series][1];
  }

  /** Retrieve the plate to which this series belongs. */
  private int getPlateNumber(int series) {
    if (plateAndWell[series][0] == -1) {
      plateAndWell[series] = getPlateAndWell(series);
    }
    return plateAndWell[series][0];
  }

  /** Retrieve a two element array containing the plate and well indices. */
  private int[] getPlateAndWell(int series) {
    // NB: Don't use FormatTools.rasterToPosition(...), because each plate
    // could have a different number of wells.
    int wellNumber = series;
    int plateNumber = 0;
    while (wellNumber >= tiffs[plateNumber].length) {
      wellNumber -= tiffs[plateNumber].length;
      plateNumber++;
    }
    return new int[] {plateNumber, wellNumber};
  }

  private byte[] getTile(int plate, int well, int no, int row, int col,
    Region intersection) throws FormatException, IOException
  {
    intersection.x %= tileWidth;
    intersection.y %= tileHeight;

    int tileIndex = (no * tileRows + row) * tileCols + col;
    String filename = getFilename(plate, well, tileIndex);
    readers[plate][well][tileIndex].setId(filename);
    byte[] buf = readers[plate][well][tileIndex].openBytes(0, intersection.x,
      intersection.y, intersection.width, intersection.height);
    return buf;
  }

  private String getFilename(int plate, int well, int tile) {
    return plate + "-" + well + "-" + tile + ".tiff";
  }

  private void unmapSeries(int plate, int well) throws IOException {
    for (int tile=0; tile<tiffs[plate][well].length; tile++) {
      Location.mapFile(getFilename(plate, well, tile), null);
      readers[plate][well][tile].close();
    }
  }

  private void preReadSeries(int plate, int well) throws IOException {
    for (int tile=0; tile<tiffs[plate][well].length; tile++) {
      String filename = getFilename(plate, well, tile);
      RandomAccessInputStream s =
        new RandomAccessInputStream(tiffs[plate][well][tile]);
      byte[] b = new byte[(int) s.length()];
      s.read(b);
      s.close();
      Location.mapFile(filename, new ByteArrayHandle(b));
    }
  }

}