//
// BaseTiffReader.java
//

/*
LOCI Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-2006 Melissa Linkert, Curtis Rueden, Chris Allan
and Eric Kjellman.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Library General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Library General Public License for more details.

You should have received a copy of the GNU Library General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.formats.in;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Hashtable;
import loci.formats.*;

/**
 * BaseTiffReader is the superclass for file format readers compatible with
 * or derived from the TIFF 6.0 file format.
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 * @author Melissa Linkert linkert at cs.wisc.edu
 */
public abstract class BaseTiffReader extends FormatReader {

  // -- Fields --

  /** Current TIFF file. */
  protected RandomAccessStream in;

  /** List of IFDs for the current TIFF. */
  protected Hashtable[] ifds;

  /** Number of images in the current TIFF stack. */
  protected int numImages;

  // -- Constructors --

  /** Constructs a new BaseTiffReader. */
  public BaseTiffReader(String name, String suffix) { super(name, suffix); }

  /** Constructs a new BaseTiffReader. */
  public BaseTiffReader(String name, String[] suffixes) {
    super(name, suffixes);
  }


  // -- BaseTiffReader API methods --

  /** Gets the dimensions of the given (possibly multi-page) TIFF file. */
  public int[] getTiffDimensions(String id)
    throws FormatException, IOException
  {
    if (!id.equals(currentId)) initFile(id);
    if (ifds == null || ifds.length == 0) return null;
    return new int[] {
      TiffTools.getIFDIntValue(ifds[0], TiffTools.IMAGE_WIDTH, false, -1),
      TiffTools.getIFDIntValue(ifds[0], TiffTools.IMAGE_LENGTH, false, -1),
      numImages
    };
  }


  // -- Internal BaseTiffReader API methods --

  /** Populates the metadata hashtable and metadata store. */
  protected void initMetadata() {
    initStandardMetadata();
    initMetadataStore();
  }

  /**
   * Parses standard metadata.
   *
   * NOTE: Absolutely <b>no</b> calls to the metadata store should be made in
   * this method or methods that override this method. Data <b>will</b> be
   * overwritten if you do so.
   */
  protected void initStandardMetadata() {
    Hashtable ifd = ifds[0];
    if (metadata == null) metadata = new Hashtable();
    put("ImageWidth", ifd, TiffTools.IMAGE_WIDTH);
    put("ImageLength", ifd, TiffTools.IMAGE_LENGTH);
    put("BitsPerSample", ifd, TiffTools.BITS_PER_SAMPLE);

    int comp = TiffTools.getIFDIntValue(ifd, TiffTools.COMPRESSION);
    String compression = null;
    switch (comp) {
      case TiffTools.UNCOMPRESSED:
        compression = "None"; break;
      case TiffTools.CCITT_1D:
        compression = "CCITT Group 3 1-Dimensional Modified Huffman"; break;
      case TiffTools.GROUP_3_FAX:
        compression = "CCITT T.4 bilevel encoding"; break;
      case TiffTools.GROUP_4_FAX:
        compression = "CCITT T.6 bilevel encoding"; break;
      case TiffTools.LZW:
        compression = "LZW"; break;
      case TiffTools.JPEG:
        compression = "JPEG"; break;
      case TiffTools.PACK_BITS:
        compression = "PackBits"; break;
    }
    put("Compression", compression);

    int photo = TiffTools.getIFDIntValue(ifd,
      TiffTools.PHOTOMETRIC_INTERPRETATION);
    String photoInterp = null;
    switch (photo) {
      case TiffTools.WHITE_IS_ZERO:
        photoInterp = "WhiteIsZero"; break;
      case TiffTools.BLACK_IS_ZERO:
        photoInterp = "BlackIsZero"; break;
      case TiffTools.RGB:
        photoInterp = "RGB"; break;
      case TiffTools.RGB_PALETTE:
        photoInterp = "Palette"; break;
      case TiffTools.TRANSPARENCY_MASK:
        photoInterp = "Transparency Mask"; break;
      case TiffTools.CMYK:
        photoInterp = "CMYK"; break;
      case TiffTools.Y_CB_CR:
        photoInterp = "YCbCr"; break;
      case TiffTools.CIE_LAB:
        photoInterp = "CIELAB"; break;
    }
    put("PhotometricInterpretation", photoInterp);

    putInt("CellWidth", ifd, TiffTools.CELL_WIDTH);
    putInt("CellLength", ifd, TiffTools.CELL_LENGTH);

    int or = TiffTools.getIFDIntValue(ifd, TiffTools.ORIENTATION);
    String orientation = null;
    // there is no case 0
    switch (or) {
      case 1: orientation = "1st row -> top; 1st column -> left"; break;
      case 2: orientation = "1st row -> top; 1st column -> right"; break;
      case 3: orientation = "1st row -> bottom; 1st column -> right"; break;
      case 4: orientation = "1st row -> bottom; 1st column -> left"; break;
      case 5: orientation = "1st row -> left; 1st column -> top"; break;
      case 6: orientation = "1st row -> right; 1st column -> top"; break;
      case 7: orientation = "1st row -> right; 1st column -> bottom"; break;
      case 8: orientation = "1st row -> left; 1st column -> bottom"; break;
    }
    put("Orientation", orientation);
    putInt("SamplesPerPixel", ifd, TiffTools.SAMPLES_PER_PIXEL);

    put("Software", ifd, TiffTools.SOFTWARE);
    put("DateTime", ifd, TiffTools.DATE_TIME);
    put("Artist", ifd, TiffTools.ARTIST);

    put("HostComputer", ifd, TiffTools.HOST_COMPUTER);
    put("Copyright", ifd, TiffTools.COPYRIGHT);

    put("NewSubfileType", ifd, TiffTools.NEW_SUBFILE_TYPE);

    int thresh = TiffTools.getIFDIntValue(ifd, TiffTools.THRESHHOLDING);
    String threshholding = null;
    switch (thresh) {
      case 1: threshholding = "No dithering or halftoning"; break;
      case 2: threshholding = "Ordered dithering or halftoning"; break;
      case 3: threshholding = "Randomized error diffusion"; break;
    }
    put("Threshholding", threshholding);

    int fill = TiffTools.getIFDIntValue(ifd, TiffTools.FILL_ORDER);
    String fillOrder = null;
    switch (fill) {
      case 1:
        fillOrder = "Pixels with lower column values are stored " +
          "in the higher order bits of a byte";
        break;
      case 2:
        fillOrder = "Pixels with lower column values are stored " +
          "in the lower order bits of a byte";
        break;
    }
    put("FillOrder", fillOrder);

    putInt("Make", ifd, TiffTools.MAKE);
    putInt("Model", ifd, TiffTools.MODEL);
    putInt("MinSampleValue", ifd, TiffTools.MIN_SAMPLE_VALUE);
    putInt("MaxSampleValue", ifd, TiffTools.MAX_SAMPLE_VALUE);
    putInt("XResolution", ifd, TiffTools.X_RESOLUTION);
    putInt("YResolution", ifd, TiffTools.Y_RESOLUTION);

    int planar = TiffTools.getIFDIntValue(ifd,
      TiffTools.PLANAR_CONFIGURATION);
    String planarConfig = null;
    switch (planar) {
      case 1: planarConfig = "Chunky"; break;
      case 2: planarConfig = "Planar"; break;
    }
    put("PlanarConfiguration", planarConfig);

    putInt("XPosition", ifd, TiffTools.X_POSITION);
    putInt("YPosition", ifd, TiffTools.Y_POSITION);
    putInt("FreeOffsets", ifd, TiffTools.FREE_OFFSETS);
    putInt("FreeByteCounts", ifd, TiffTools.FREE_BYTE_COUNTS);
    putInt("GrayResponseUnit", ifd, TiffTools.GRAY_RESPONSE_UNIT);
    putInt("GrayResponseCurve", ifd, TiffTools.GRAY_RESPONSE_CURVE);
    putInt("T4Options", ifd, TiffTools.T4_OPTIONS);
    putInt("T6Options", ifd, TiffTools.T6_OPTIONS);

    int res = TiffTools.getIFDIntValue(ifd, TiffTools.RESOLUTION_UNIT);
    String resUnit = null;
    switch (res) {
      case 1: resUnit = "None"; break;
      case 2: resUnit = "Inch"; break;
      case 3: resUnit = "Centimeter"; break;
    }
    put("ResolutionUnit", resUnit);

    putInt("PageNumber", ifd, TiffTools.PAGE_NUMBER);
    putInt("TransferFunction", ifd, TiffTools.TRANSFER_FUNCTION);

    int predict = TiffTools.getIFDIntValue(ifd, TiffTools.PREDICTOR);
    String predictor = null;
    switch (predict) {
      case 1: predictor = "No prediction scheme"; break;
      case 2: predictor = "Horizontal differencing"; break;
    }
    put("Predictor", predictor);

    putInt("WhitePoint", ifd, TiffTools.WHITE_POINT);
    putInt("PrimaryChromacities", ifd, TiffTools.PRIMARY_CHROMATICITIES);

    putInt("HalftoneHints", ifd, TiffTools.HALFTONE_HINTS);
    putInt("TileWidth", ifd, TiffTools.TILE_WIDTH);
    putInt("TileLength", ifd, TiffTools.TILE_LENGTH);
    putInt("TileOffsets", ifd, TiffTools.TILE_OFFSETS);
    putInt("TileByteCounts", ifd, TiffTools.TILE_BYTE_COUNTS);

    int ink = TiffTools.getIFDIntValue(ifd, TiffTools.INK_SET);
    String inkSet = null;
    switch (ink) {
      case 1: inkSet = "CMYK"; break;
      case 2: inkSet = "Other"; break;
    }
    put("InkSet", inkSet);

    putInt("InkNames", ifd, TiffTools.INK_NAMES);
    putInt("NumberOfInks", ifd, TiffTools.NUMBER_OF_INKS);
    putInt("DotRange", ifd, TiffTools.DOT_RANGE);
    put("TargetPrinter", ifd, TiffTools.TARGET_PRINTER);
    putInt("ExtraSamples", ifd, TiffTools.EXTRA_SAMPLES);

    int fmt = TiffTools.getIFDIntValue(ifd, TiffTools.SAMPLE_FORMAT);
    String sampleFormat = null;
    switch (fmt) {
      case 1: sampleFormat = "unsigned integer"; break;
      case 2: sampleFormat = "two's complement signed integer"; break;
      case 3: sampleFormat = "IEEE floating point"; break;
      case 4: sampleFormat = "undefined"; break;
    }
    put("SampleFormat", sampleFormat);

    putInt("SMinSampleValue", ifd, TiffTools.S_MIN_SAMPLE_VALUE);
    putInt("SMaxSampleValue", ifd, TiffTools.S_MAX_SAMPLE_VALUE);
    putInt("TransferRange", ifd, TiffTools.TRANSFER_RANGE);

    int jpeg = TiffTools.getIFDIntValue(ifd, TiffTools.JPEG_PROC);
    String jpegProc = null;
    switch (jpeg) {
      case 1: jpegProc = "baseline sequential process"; break;
      case 14: jpegProc = "lossless process with Huffman coding"; break;
    }
    put("JPEGProc", jpegProc);

    putInt("JPEGInterchangeFormat", ifd, TiffTools.JPEG_INTERCHANGE_FORMAT);
    putInt("JPEGRestartInterval", ifd, TiffTools.JPEG_RESTART_INTERVAL);

    putInt("JPEGLosslessPredictors",
      ifd, TiffTools.JPEG_LOSSLESS_PREDICTORS);
    putInt("JPEGPointTransforms", ifd, TiffTools.JPEG_POINT_TRANSFORMS);
    putInt("JPEGQTables", ifd, TiffTools.JPEG_Q_TABLES);
    putInt("JPEGDCTables", ifd, TiffTools.JPEG_DC_TABLES);
    putInt("JPEGACTables", ifd, TiffTools.JPEG_AC_TABLES);
    putInt("YCbCrCoefficients", ifd, TiffTools.Y_CB_CR_COEFFICIENTS);

    int ycbcr = TiffTools.getIFDIntValue(ifd,
      TiffTools.Y_CB_CR_SUB_SAMPLING);
    String subSampling = null;
    switch (ycbcr) {
      case 1:
        subSampling = "chroma image dimensions = luma image dimensions";
        break;
      case 2:
        subSampling = "chroma image dimensions are " +
          "half the luma image dimensions";
        break;
      case 4:
        subSampling = "chroma image dimensions are " +
          "1/4 the luma image dimensions";
        break;
    }
    put("YCbCrSubSampling", subSampling);

    putInt("YCbCrPositioning", ifd, TiffTools.Y_CB_CR_POSITIONING);
    putInt("ReferenceBlackWhite", ifd, TiffTools.REFERENCE_BLACK_WHITE);

    // bits per sample and number of channels
    Object bpsObj = TiffTools.getIFDValue(ifd, TiffTools.BITS_PER_SAMPLE);
    int bps = -1, numC = 3;
    if (bpsObj instanceof int[]) {
      int[] q = (int[]) bpsObj;
      bps = q[0];
      numC = q.length;
    }
    else if (bpsObj instanceof Number) {
      bps = ((Number) bpsObj).intValue();
      numC = 1;
    }

    // numC isn't set properly if we have an indexed color image, so we need
    // to reset it here

    int p = TiffTools.getIFDIntValue(ifd, TiffTools.PHOTOMETRIC_INTERPRETATION);
    if (p == TiffTools.RGB_PALETTE) {
      numC = 3;
      bps *= 3;
    }

    put("BitsPerSample", bps);
    put("NumberOfChannels", numC);

    // TIFF comment
    String comment = null;
    Object o = TiffTools.getIFDValue(ifd, TiffTools.IMAGE_DESCRIPTION);
    if (o instanceof String) comment = (String) o;
    else if (o instanceof String[]) {
      String[] s = (String[]) o;
      if (s.length > 0) comment = s[0];
    }
    else if (o != null) comment = o.toString();
    if (comment != null) {
      // sanitize comment
      comment = comment.replaceAll("\r\n", "\n"); // CR-LF to LF
      comment = comment.replaceAll("\r", "\n"); // CR to LF
      put("Comment", comment);
    }
  }

  /**
   * Populates the metadata store using the data parsed in
   * {@link #initStandardMetadata()} along with some further parsing done in
   * the method itself.
   *
   * All calls to the active <code>MetadataStore</code> should be made in this
   * method and <b>only</b> in this method. This is especially important for
   * sub-classes that override the getters for pixel set array size, etc.
   */
  protected void initMetadataStore() {
    Hashtable ifd = ifds[0];
    try {
      // Set the pixel values in the metadata store.
      setPixels();

      // The metadata store we're working with.
      MetadataStore store = getMetadataStore(currentId);

      // populate Experimenter element
      String artist = (String) TiffTools.getIFDValue(ifd, TiffTools.ARTIST);
      if (artist != null) {
        String firstName = null, lastName = null;
        int ndx = artist.indexOf(" ");
        if (ndx < 0) lastName = artist;
        else {
          firstName = artist.substring(0, ndx);
          lastName = artist.substring(ndx + 1);
        }
        String email = (String)
          TiffTools.getIFDValue(ifd, TiffTools.HOST_COMPUTER);
        store.setExperimenter(firstName, lastName, email,
                              null, null, null, null);
      }

      // populate Image element
      setImage();

      // populate Dimensions element
      int pixelSizeX = TiffTools.getIFDIntValue(ifd,
        TiffTools.CELL_WIDTH, false, 0);
      int pixelSizeY = TiffTools.getIFDIntValue(ifd,
        TiffTools.CELL_LENGTH, false, 0);
      int pixelSizeZ = TiffTools.getIFDIntValue(ifd,
        TiffTools.ORIENTATION, false, 0);
      store.setDimensions(new Float(pixelSizeX), new Float(pixelSizeY),
                          new Float(pixelSizeZ), null, null, null);

//      OMETools.setAttribute(ome, "ChannelInfo", "SamplesPerPixel", "" +
//        TiffTools.getIFDIntValue(ifd, TiffTools.SAMPLES_PER_PIXEL));

//      int photoInterp2 = TiffTools.getIFDIntValue(ifd,
//        TiffTools.PHOTOMETRIC_INTERPRETATION, true, 0);
//      String photo2;
//      switch (photoInterp2) {
//        case 0: photo2 = "monochrome"; break;
//        case 1: photo2 = "monochrome"; break;
//        case 2: photo2 = "RGB"; break;
//        case 3: photo2 = "monochrome"; break;
//        case 4: photo2 = "RGB"; break;
//        default: photo2 = unknown;
//      }
//      OMETools.setAttribute(ome, "ChannelInfo",
//        "PhotometricInterpretation", photo2);

      // populate StageLabel element

      Object x = TiffTools.getIFDValue(ifd, TiffTools.X_POSITION);
      Object y = TiffTools.getIFDValue(ifd, TiffTools.Y_POSITION);
      Float stageX;
      Float stageY;
      if (x instanceof TiffRational) {
        stageX = x == null ? null : new Float(((TiffRational) x).floatValue());
        stageY = y == null ? null : new Float(((TiffRational) y).floatValue());
      }
      else {
        stageX = x == null ? null : new Float((String) x);
        stageY = y == null ? null : new Float((String) y);
      }
      store.setStageLabel(null, stageX, stageY, null, null);

      // populate Instrument element
      String model = (String) TiffTools.getIFDValue(ifd, TiffTools.MODEL);
      String serialNumber = (String)
        TiffTools.getIFDValue(ifd, TiffTools.MAKE);
      store.setInstrument(null, model, serialNumber, null, null);
    }
    catch (FormatException exc) { exc.printStackTrace(); }
    catch (IOException ex) { ex.printStackTrace(); }
  }


  // -- FormatReader API methods --

  /** Checks if the given block is a valid header for a TIFF file. */
  public boolean isThisType(byte[] block) {
    return TiffTools.isValidHeader(block);
  }

  /** Determines the number of images in the given TIFF file. */
  public int getImageCount(String id) throws FormatException, IOException {
    if (!id.equals(currentId)) initFile(id);
    return (!isRGB(id) || !separated) ? numImages : 3 * numImages;
  }

  /** Checks if the images in the file are RGB. */
  public boolean isRGB(String id) throws FormatException, IOException {
    if (!id.equals(currentId)) initFile(id);
    return (TiffTools.getIFDIntValue(ifds[0],
      TiffTools.SAMPLES_PER_PIXEL, false, 1) > 1) ||
      (TiffTools.getIFDIntValue(ifds[0], TiffTools.PHOTOMETRIC_INTERPRETATION,
      true, 0) == TiffTools.RGB_PALETTE);
  }

  /**
   * Obtains the specified metadata field's value for the given file.
   *
   * @param field the name associated with the metadata field
   * @return the value, or null if the field doesn't exist
   */
  public Object getMetadataValue(String id, String field)
    throws FormatException, IOException
  {
    if (!id.equals(currentId) && !DataTools.samePrefix(id, currentId)) {
      initFile(id);
    }
    return metadata.get(field);
  }

  /** Get the size of the X dimension. */
  public int getSizeX(String id) throws FormatException, IOException {
    if (!id.equals(currentId)) initFile(id);
    return TiffTools.getIFDIntValue(ifds[0], TiffTools.IMAGE_WIDTH, false, 0);
  }

  /** Get the size of the Y dimension. */
  public int getSizeY(String id) throws FormatException, IOException {
    if (!id.equals(currentId)) initFile(id);
    return TiffTools.getIFDIntValue(ifds[0], TiffTools.IMAGE_LENGTH, false, 0);
  }

  /** Get the size of the Z dimension. */
  public int getSizeZ(String id) throws FormatException, IOException {
    return 1;
  }

  /** Get the size of the C dimension. */
  public int getSizeC(String id) throws FormatException, IOException {
    if (!id.equals(currentId)) initFile(id);
    return isRGB(id) ? 3 : 1;
  }

  /** Get the size of the T dimension. */
  public int getSizeT(String id) throws FormatException, IOException {
    if (!id.equals(currentId)) initFile(id);
    return ifds.length;
  }

  /** Return true if the data is in little-endian format. */
  public boolean isLittleEndian(String id) throws FormatException, IOException {
    if (!id.equals(currentId)) initFile(id);
    return TiffTools.isLittleEndian(ifds[0]);
  }

  /**
   * Return a five-character string representing the dimension order
   * within the file.
   */
  public String getDimensionOrder(String id) throws FormatException, IOException
  {
    return "XYCZT";
  }

  /** Obtains the specified image from the given TIFF file as a byte array. */
  public byte[] openBytes(String id, int no)
    throws FormatException, IOException
  {
    return ImageTools.getBytes(openImage(id, no), isRGB(id) && separated,
      no % 3);
  }

  /** Obtains the specified image from the given TIFF file. */
  public BufferedImage openImage(String id, int no)
    throws FormatException, IOException
  {
    if (!id.equals(currentId) && !DataTools.samePrefix(id, currentId)) {
      initFile(id);
    }

    if (no < 0 || no >= getImageCount(id)) {
      throw new FormatException("Invalid image number: " + no);
    }

    if (!isRGB(id) || !separated) {
      return TiffTools.getImage(ifds[no], in);
    }
    else {
      BufferedImage[] channels =
        ImageTools.splitChannels(TiffTools.getImage(ifds[no / 3], in));
      return channels[no % channels.length];
    }
  }

  /** Closes any open files. */
  public void close() throws FormatException, IOException {
    if (in != null) in.close();
    in = null;
    currentId = null;
  }

  /** Initializes the given TIFF file. */
  protected void initFile(String id) throws FormatException, IOException {
    super.initFile(id);
    in = new RandomAccessStream(id);
    if (in.readShort() == 0x4949) in.order(true);

    ifds = TiffTools.getIFDs(in);
    if (ifds == null) throw new FormatException("No IFDs found");
    numImages = ifds.length;
    initMetadata();
  }

  /**
   * If the TIFF is big-endian.
   * @return <code>true</code> if the TIFF is big-endian, <code>false</code>
   * otherwise.
   * @throws FormatException if there is a problem parsing this metadata.
   */
  protected Boolean getBigEndian() throws FormatException {
    return new Boolean(!TiffTools.isLittleEndian(ifds[0]));
  }

  /**
   * Retrieves the pixel type from the TIFF.
   * @return the pixel type
   */
  protected String getPixelType() throws FormatException {
    Hashtable ifd = ifds[0];
    int sample = TiffTools.getIFDIntValue(ifd, TiffTools.SAMPLE_FORMAT);
    String pixelType;
    switch (sample) {
      case 1: pixelType = "int"; break;
      case 2: pixelType = "Uint"; break;
      case 3: pixelType = "float"; break;
      default: pixelType = "unknown";
    }
    if (pixelType.indexOf("int") >= 0) { // int or Uint
      pixelType += TiffTools.getIFDIntValue(ifd, TiffTools.BITS_PER_SAMPLE);
    }
    return pixelType;
  }

  /**
   * Performs the actual setting of the pixels attributes in the active metadata
   * store by calling:
   *
   * <ul>
   *   <li>{@link #getSizeX()}</li>
   *   <li>{@link #getSizeY()}</li>
   *   <li>{@link #getSizeZ()}</li>
   *   <li>{@link #getSizeC()}</li>
   *   <li>{@link #getSizeT()}</li>
   *   <li>{@link #getPixelType()}</li>
   *   <li>{@link #getDimensionOrder()}</li>
   *   <li>{@link #getBigEndian()}</li>
   * </ul>
   *
   * If the retrieval of any of these attributes is non-standard, the sub-class
   * should override the corresponding method.
   * @throws FormatException if there is a problem parsing any of the
   * attributes.
   */
  private void setPixels() throws FormatException, IOException {
    getMetadataStore(currentId).setPixels(
      new Integer(getSizeX(currentId)), new Integer(getSizeY(currentId)),
      new Integer(getSizeZ(currentId)), new Integer(getSizeC(currentId)),
      new Integer(getSizeT(currentId)), getPixelType(),
      getBigEndian(), getDimensionOrder(currentId), null);
  }

  /**
   * Performs the actual setting of the image attributes in the active metadata
   * store by calling:
   *
   * <ul>
   *   <li>{@link #getImageName()}</li>
   *   <li>{@link #getImageCreationDate()}</li>
   *   <li>{@link #getgetImageDescription()}</li>
   * </ul>
   *
   * If the retrieval of any of these attributes is non-standard, the sub-class
   * should override the corresponding method.
   * @throws FormatException if there is a problem parsing any of the
   * attributes.
   */
  private void setImage() throws FormatException, IOException {
    getMetadataStore(currentId).setImage(getImageName(), getImageCreationDate(),
                                getImageDescription(), null);
  }

  /**
   * Retrieves the image name from the TIFF.
   * @return the image name.
   */
  protected String getImageName() {
    return null;
  }


  /**
   * Retrieves the image creation date.
   * @return the image creation date.
   */
  protected String getImageCreationDate() {
    return (String) TiffTools.getIFDValue(ifds[0], TiffTools.DATE_TIME);
  }

  /**
   * Retrieves the image description.
   * @return the image description.
   */
  protected String getImageDescription() {
    return (String) metadata.get("Comment");
  }

  // -- Helper methods --

  protected void put(String key, Object value) {
    if (value == null) return;
    metadata.put(key, value);
  }

  protected void put(String key, int value) {
    if (value == -1) return; // indicates missing value
      metadata.put(key, new Integer(value));
    }

  protected void put(String key, boolean value) {
    put(key, new Boolean(value));
  }
  protected void put(String key, byte value) { put(key, new Byte(value)); }
  protected void put(String key, char value) {
    put(key, new Character(value));
  }
  protected void put(String key, double value) { put(key, new Double(value)); }
  protected void put(String key, float value) { put(key, new Float(value)); }
  protected void put(String key, long value) { put(key, new Long(value)); }
  protected void put(String key, short value) { put(key, new Short(value)); }

  protected void put(String key, Hashtable ifd, int tag) {
    put(key, TiffTools.getIFDValue(ifd, tag));
  }

  protected void putInt(String key, Hashtable ifd, int tag) {
    put(key, TiffTools.getIFDIntValue(ifd, tag));
  }
}
