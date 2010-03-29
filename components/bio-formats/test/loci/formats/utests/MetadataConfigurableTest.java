//
// MetadataConfigurableTest.java
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

package loci.formats.utests;

import static org.testng.AssertJUnit.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;

import loci.common.DataTools;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/test/loci/formats/utests/MetadataConfigurableTest.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/test/loci/formats/utests/MetadataConfigurabletest.java">SVN</a></dd></dl>
 */
public class MetadataConfigurableTest {

  private static final String FILENAME_PROPERTY = "testng.filename";

  private ImageReader pixelsOnly;
  private ImageReader all;
  private String id;

  @BeforeClass
  public void setUp() {
    pixelsOnly = new ImageReader();
    pixelsOnly.setMetadataOptions(
      new DefaultMetadataOptions(MetadataLevel.MINIMUM));
    all = new ImageReader();
    all.setMetadataOptions(new DefaultMetadataOptions(MetadataLevel.ALL));
    id = System.getProperty(FILENAME_PROPERTY);
  }

  @Test
  public void testSetId() throws FormatException, IOException {
    long t0 = System.currentTimeMillis();
    pixelsOnly.setId(id);
    assertEquals(MetadataLevel.MINIMUM,
      pixelsOnly.getMetadataOptions().getMetadataLevel());

    long t1 = System.currentTimeMillis();
    all.setId(id);
    assertEquals(MetadataLevel.ALL,
      all.getMetadataOptions().getMetadataLevel());
    assertFalse(0 ==
      all.getSeriesMetadata().size() + all.getGlobalMetadata().size());

    long t2 = System.currentTimeMillis();
    System.err.println(String.format("Pixels only: %d -- All: %d",
      t1 - t0, t2 - t1));
  }

  @Test(dependsOnMethods={"testSetId"})
  public void testDimensions() {
    assertEquals(all.getSeriesCount(), pixelsOnly.getSeriesCount());
    for (int i=0; i<pixelsOnly.getSeriesCount(); i++) {
      all.setSeries(i);
      pixelsOnly.setSeries(i);

      assertEquals(all.getSizeX(), pixelsOnly.getSizeX());
      assertEquals(all.getSizeY(), pixelsOnly.getSizeY());
      assertEquals(all.getSizeZ(), pixelsOnly.getSizeZ());
      assertEquals(all.getSizeC(), pixelsOnly.getSizeC());
      assertEquals(all.getSizeT(), pixelsOnly.getSizeT());
      assertEquals(all.getPixelType(), pixelsOnly.getPixelType());
      assertEquals(all.isLittleEndian(), pixelsOnly.isLittleEndian());
    }
  }

  @Test(dependsOnMethods={"testSetId"})
  public void testPlaneData() throws FormatException, IOException {
    for (int i=0; i<pixelsOnly.getSeriesCount(); i++) {
      pixelsOnly.setSeries(i);
      all.setSeries(i);
      assertEquals(all.getImageCount(), pixelsOnly.getImageCount());
      for (int j=0; j<pixelsOnly.getImageCount(); j++) {
        byte[] pixelsOnlyPlane = pixelsOnly.openBytes(j);
        String pixelsOnlySHA1 = sha1(pixelsOnlyPlane);
        byte[] allPlane = all.openBytes(j);
        String allSHA1 = sha1(allPlane);

        if (!pixelsOnlySHA1.equals(allSHA1)) {
          fail(String.format("MISMATCH: Series:%d Image:%d PixelsOnly%s All:%s",
            i, j, pixelsOnlySHA1, allSHA1));
        }
      }
    }
  }

  @Test(dependsOnMethods={"testSetId"})
  public void testUsedFiles() throws FormatException, IOException {
    for (int i=0; i<pixelsOnly.getSeriesCount(); i++) {
      pixelsOnly.setSeries(i);
      all.setSeries(i);

      String[] pixelsOnlyFiles = pixelsOnly.getSeriesUsedFiles();
      String[] allFiles = all.getSeriesUsedFiles();

      assertEquals(allFiles.length, pixelsOnlyFiles.length);

      Arrays.sort(allFiles);
      Arrays.sort(pixelsOnlyFiles);

      for (int j=0; j<pixelsOnlyFiles.length; j++) {
        assertEquals(allFiles[j], pixelsOnlyFiles[j]);
      }
    }
  }

  // -- Utility methods --

  private String sha1(byte[] buf) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      return DataTools.bytesToHex(md.digest(buf));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
