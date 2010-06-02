//
// ReaderWrapper.java
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

package loci.formats;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Set;

import loci.common.RandomAccessInputStream;
import loci.formats.in.MetadataLevel;
import loci.formats.in.MetadataOptions;
import loci.formats.meta.MetadataStore;

/**
 * Abstract superclass of reader logic that wraps other readers.
 * All methods are simply delegated to the wrapped reader.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/ReaderWrapper.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/ReaderWrapper.java">SVN</a></dd></dl>
 */
public abstract class ReaderWrapper implements IFormatReader {

  // -- Fields --

  /** FormatReader used to read the file. */
  protected IFormatReader reader;

  // -- Constructors --

  /** Constructs a reader wrapper around a new image reader. */
  public ReaderWrapper() { this(new ImageReader()); }

  /** Constructs a reader wrapper around the given reader. */
  public ReaderWrapper(IFormatReader r) {
    if (r == null) {
      throw new IllegalArgumentException("Format reader cannot be null");
    }
    reader = r;
  }

  // -- ReaderWrapper API methods --

  /** Gets the wrapped reader. */
  public IFormatReader getReader() { return reader; }

  /**
   * Unwraps nested wrapped readers until the core reader (i.e., not
   * a {@link ReaderWrapper} or {@link ImageReader}) is found.
   */
  public IFormatReader unwrap() throws FormatException, IOException {
    return unwrap(null, null);
  }

  /**
   * Unwraps nested wrapped readers until the core reader (i.e., not
   * a {@link ReaderWrapper} or {@link ImageReader}) is found.
   *
   * @param id Id to use as a basis when unwrapping any nested
   *   {@link ImageReader}s. If null, the current id is used.
   */
  public IFormatReader unwrap(String id)
    throws FormatException, IOException
  {
    return unwrap(null, id);
  }

  /**
   * Unwraps nested wrapped readers until the given reader class is found.
   *
   * @param readerClass Class of the desired nested reader. If null, the
   *   core reader (i.e., deepest wrapped reader) will be returned.
   * @param id Id to use as a basis when unwrapping any nested
   *   {@link ImageReader}s. If null, the current id is used.
   */
  public IFormatReader unwrap(Class<? extends IFormatReader> readerClass,
    String id) throws FormatException, IOException
  {
    IFormatReader r = this;
    while (r instanceof ReaderWrapper || r instanceof ImageReader) {
      if (readerClass != null && readerClass.isInstance(r)) break;
      if (r instanceof ImageReader) {
        ImageReader ir = (ImageReader) r;
        r = id == null ? ir.getReader() : ir.getReader(id);
      }
      else r = ((ReaderWrapper) r).getReader();
    }
    if (readerClass != null && !readerClass.isInstance(r)) return null;
    return r;
  }

  /**
   * Performs a deep copy of the reader, including nested wrapped readers.
   * Most of the reader state is preserved as well, including:<ul>
   *   <li>{@link #isNormalized()}</li>
   *   <li>{@link #isMetadataFiltered()}</li>
   *   <li>{@link #isMetadataCollected()}</li>
   *   <li>{@link DelegateReader#isLegacy()}</li>
   * </ul>
   *
   * @param imageReaderClass If non-null, any {@link ImageReader}s in the
   *   reader stack will be replaced with instances of the given class.
   * @throws FormatException If something goes wrong during the duplication.
   */
  public ReaderWrapper duplicate(
    Class<? extends IFormatReader> imageReaderClass) throws FormatException
  {
    ReaderWrapper wrapperCopy = duplicateRecurse(imageReaderClass);

    // sync top-level configuration with original reader
    boolean normalized = isNormalized();
    boolean metadataFiltered = isMetadataFiltered();
    boolean metadataCollected = isMetadataCollected();
    wrapperCopy.setNormalized(normalized);
    wrapperCopy.setMetadataFiltered(metadataFiltered);
    wrapperCopy.setMetadataCollected(metadataCollected);
    return wrapperCopy;
  }

  // -- IMetadataConfigurable API methods --

  /* (non-Javadoc)
   * @see loci.formats.IMetadataConfigurable#getSupportedMetadataLevels()
   */
  public Set<MetadataLevel> getSupportedMetadataLevels() {
    return reader.getSupportedMetadataLevels();
  }

  /* (non-Javadoc)
   * @see loci.formats.IMetadataConfigurable#getMetadataOptions()
   */
  public MetadataOptions getMetadataOptions() {
    return reader.getMetadataOptions();
  }

  /* (non-Javadoc)
   * @see loci.formats.IMetadataConfigurable#setMetadataOptions(loci.formats.in.MetadataOptions)
   */
  public void setMetadataOptions(MetadataOptions options) {
    reader.setMetadataOptions(options);
  }

  // -- IFormatReader API methods --

  public boolean isThisType(String name, boolean open) {
    return reader.isThisType(name, open);
  }

  public boolean isThisType(byte[] block) {
    return reader.isThisType(block);
  }

  public boolean isThisType(RandomAccessInputStream stream) throws IOException{
    return reader.isThisType(stream);
  }

  public int getImageCount() {
    return reader.getImageCount();
  }

  public boolean isRGB() {
    return reader.isRGB();
  }

  public int getSizeX() {
    return reader.getSizeX();
  }

  public int getSizeY() {
    return reader.getSizeY();
  }

  public int getSizeZ() {
    return reader.getSizeZ();
  }

  public int getSizeC() {
    return reader.getSizeC();
  }

  public int getSizeT() {
    return reader.getSizeT();
  }

  public int getPixelType() {
    return reader.getPixelType();
  }

  public int getBitsPerPixel() {
    return reader.getBitsPerPixel();
  }

  public int getEffectiveSizeC() {
    //return reader.getEffectiveSizeC();
    int sizeZT = getSizeZ() * getSizeT();
    if (sizeZT == 0) return 0;
    return getImageCount() / sizeZT;
  }

  public int getRGBChannelCount() {
    //return reader.getRGBChannelCount();
    int effSizeC = getEffectiveSizeC();
    if (effSizeC == 0) return 0;
    return getSizeC() / effSizeC;
  }

  public boolean isIndexed() {
    return reader.isIndexed();
  }

  public boolean isFalseColor() {
    return reader.isFalseColor();
  }

  public byte[][] get8BitLookupTable() throws FormatException, IOException {
    return reader.get8BitLookupTable();
  }

  public short[][] get16BitLookupTable() throws FormatException, IOException {
    return reader.get16BitLookupTable();
  }

  public int[] getChannelDimLengths() {
    return reader.getChannelDimLengths();
  }

  public String[] getChannelDimTypes() {
    return reader.getChannelDimTypes();
  }

  public int getThumbSizeX() {
    return reader.getThumbSizeX();
  }

  public int getThumbSizeY() {
    return reader.getThumbSizeY();
  }

  public boolean isLittleEndian() {
    return reader.isLittleEndian();
  }

  public String getDimensionOrder() {
    return reader.getDimensionOrder();
  }

  public boolean isOrderCertain() {
    return reader.isOrderCertain();
  }

  public boolean isThumbnailSeries() {
    return reader.isThumbnailSeries();
  }

  public boolean isInterleaved() {
    return reader.isInterleaved();
  }

  public boolean isInterleaved(int subC) {
    return reader.isInterleaved(subC);
  }

  public byte[] openBytes(int no) throws FormatException, IOException {
    return reader.openBytes(no);
  }

  public byte[] openBytes(int no, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    return reader.openBytes(no, x, y, w, h);
  }

  public byte[] openBytes(int no, byte[] buf)
    throws FormatException, IOException
  {
    return reader.openBytes(no, buf);
  }

  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    return reader.openBytes(no, buf, x, y, w, h);
  }

  public Object openPlane(int no, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    return reader.openPlane(no, x, y, w, h);
  }

  public byte[] openThumbBytes(int no) throws FormatException, IOException {
    return reader.openThumbBytes(no);
  }

  public void close(boolean fileOnly) throws IOException {
    reader.close(fileOnly);
  }

  public int getSeriesCount() {
    return reader.getSeriesCount();
  }

  public void setSeries(int no) {
    reader.setSeries(no);
  }

  public int getSeries() {
    return reader.getSeries();
  }

  public void setGroupFiles(boolean group) {
    reader.setGroupFiles(group);
  }

  public boolean isGroupFiles() {
    return reader.isGroupFiles();
  }

  public int fileGroupOption(String id) throws FormatException, IOException {
    return reader.fileGroupOption(id);
  }

  public boolean isMetadataComplete() {
    return reader.isMetadataComplete();
  }

  public void setNormalized(boolean normalize) {
    reader.setNormalized(normalize);
  }

  public boolean isNormalized() { return reader.isNormalized(); }

  public void setOriginalMetadataPopulated(boolean populate) {
    reader.setOriginalMetadataPopulated(populate);
  }

  public boolean isOriginalMetadataPopulated() {
    return reader.isOriginalMetadataPopulated();
  }

  public String[] getUsedFiles() {
    return reader.getUsedFiles();
  }

  public String[] getUsedFiles(boolean noPixels) {
    return reader.getUsedFiles(noPixels);
  }

  public String[] getSeriesUsedFiles() {
    return reader.getSeriesUsedFiles();
  }

  public String[] getSeriesUsedFiles(boolean noPixels) {
    return reader.getSeriesUsedFiles(noPixels);
  }

  public FileInfo[] getAdvancedUsedFiles(boolean noPixels) {
    return reader.getAdvancedUsedFiles(noPixels);
  }

  public FileInfo[] getAdvancedSeriesUsedFiles(boolean noPixels) {
    return reader.getAdvancedSeriesUsedFiles(noPixels);
  }

  public String getCurrentFile() { return reader.getCurrentFile(); }

  public int getIndex(int z, int c, int t) {
    return reader.getIndex(z, c, t);
  }

  public int[] getZCTCoords(int index) {
    return reader.getZCTCoords(index);
  }

  public Object getMetadataValue(String field) {
    return reader.getMetadataValue(field);
  }

  public Hashtable<String, Object> getGlobalMetadata() {
    return reader.getGlobalMetadata();
  }

  public Hashtable<String, Object> getSeriesMetadata() {
    return reader.getSeriesMetadata();
  }

  public CoreMetadata[] getCoreMetadata() {
    //return reader.getCoreMetadata();

    // NB: Be sure all CoreMetadata values are returned correctly,
    // regardless of any method overrides.
    return copyCoreMetadata(this);
  }

  public void setMetadataFiltered(boolean filter) {
    reader.setMetadataFiltered(filter);
  }

  public boolean isMetadataFiltered() { return reader.isMetadataFiltered(); }

  public void setMetadataStore(MetadataStore store) {
    reader.setMetadataStore(store);
  }

  public MetadataStore getMetadataStore() {
    return reader.getMetadataStore();
  }

  public Object getMetadataStoreRoot() {
    return reader.getMetadataStoreRoot();
  }

  public IFormatReader[] getUnderlyingReaders() {
    return new IFormatReader[] {reader};
  }

  public boolean isSingleFile(String id) throws FormatException, IOException {
    return reader.isSingleFile(id);
  }

  public boolean hasCompanionFiles() {
    return reader.hasCompanionFiles();
  }

  public String[] getPossibleDomains(String id)
    throws FormatException, IOException
  {
    return reader.getPossibleDomains(id);
  }

  public String[] getDomains() {
    return reader.getDomains();
  }

  // -- IFormatHandler API methods --

  public boolean isThisType(String name) {
    return reader.isThisType(name);
  }

  public String getFormat() {
    return reader.getFormat();
  }

  public String[] getSuffixes() {
    return reader.getSuffixes();
  }

  public Class<?> getNativeDataType() {
    return reader.getNativeDataType();
  }

  public void setId(String id) throws FormatException, IOException {
    reader.setId(id);
  }

  public void close() throws IOException {
    reader.close();
  }

  // -- Helper methods --

  private ReaderWrapper duplicateRecurse(
    Class<? extends IFormatReader> imageReaderClass) throws FormatException
  {
    IFormatReader childCopy = null;
    if (reader instanceof ReaderWrapper) {
      // found a nested reader layer; duplicate via recursion
      childCopy = ((ReaderWrapper) reader).duplicateRecurse(imageReaderClass);
    }
    else {
      Class<? extends IFormatReader> c = null;
      if (reader instanceof ImageReader) {
        // found an image reader; if given, substitute the reader class
        c = imageReaderClass == null ? ImageReader.class : imageReaderClass;
      }
      else {
        // bottom of the reader stack; duplicate the core reader
        c = reader.getClass();
      }
      try {
        childCopy = c.newInstance();
      }
      catch (IllegalAccessException exc) { throw new FormatException(exc); }
      catch (InstantiationException exc) { throw new FormatException(exc); }

      // preserve reader-specific configuration with original reader
      if (reader instanceof DelegateReader) {
        DelegateReader delegateOriginal = (DelegateReader) reader;
        DelegateReader delegateCopy = (DelegateReader) childCopy;
        delegateCopy.setLegacy(delegateOriginal.isLegacy());
      }
    }

    // use crazy reflection to instantiate a reader of the proper type
    Class<? extends ReaderWrapper> wrapperClass = getClass();
    ReaderWrapper wrapperCopy = null;
    try {
      wrapperCopy = wrapperClass.getConstructor(new Class[]
        {IFormatReader.class}).newInstance(new Object[] {childCopy});
    }
    catch (InstantiationException exc) { throw new FormatException(exc); }
    catch (IllegalAccessException exc) { throw new FormatException(exc); }
    catch (NoSuchMethodException exc) { throw new FormatException(exc); }
    catch (InvocationTargetException exc) { throw new FormatException(exc); }

    return wrapperCopy;
  }

  // -- Deprecated methods --


  /** @deprecated */
  public void setMetadataCollected(boolean collect) {
    reader.setMetadataCollected(collect);
  }

  /** @deprecated */
  public boolean isMetadataCollected() { return reader.isMetadataCollected(); }

  /** @deprecated */
  public Hashtable<String, Object> getMetadata() {
    return reader.getMetadata();
  }

  // -- Helper methods --

  /** Creates a copy of the core metadata matching to the given reader state. */
  protected CoreMetadata[] copyCoreMetadata(IFormatReader r) {
    CoreMetadata[] core = new CoreMetadata[r.getSeriesCount()];
    for (int s=0; s<core.length; s++) core[s] = new CoreMetadata(r, s);
    return core;
  }

}
