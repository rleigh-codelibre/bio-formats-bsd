//
// OMEReader.java
//

/*
LOCI Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ Melissa Linkert, Curtis Rueden, Chris Allan,
Eric Kjellman and Brian Loranger.

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

package loci.formats.ome;

import java.awt.image.BufferedImage;
import java.io.*;
import loci.formats.*;
import org.openmicroscopy.ds.*;
import org.openmicroscopy.ds.dto.*;
import org.openmicroscopy.ds.st.*;
import org.openmicroscopy.is.*;

/**
 * OMEReader retrieves images on demand from an OME database.
 * Authentication with the OME server is handled, provided the 'id' parameter
 * is properly formed.
 * The 'id' parameter should take one of the following forms:
 *
 * [server]?user=[username]&password=[password]&id=[image id]
 * [server]?key=[session key]&id=[image id]
 *
 * where [server] is the URL of the OME data server (not the image server).
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/loci/formats/ome/OMEReader.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/loci/formats/ome/OMEReader.java">SVN</a></dd></dl>
 */
public class OMEReader extends FormatReader {

  // -- Fields --

  /** String containing authentication information. */
  private String loginString;

  /** Current server. */
  private String server;

  /** Session key for authentication. */
  private String sessionKey;

  /** OME image ID. */
  private String imageId;

  /** Thumbnail associated with the current dataset. */
  private BufferedImage thumb;

  /** Download helpers. */
  private DataServices rs;
  private RemoteCaller rc;
  private DataFactory df;
  private PixelsFactory pf;
  private Pixels pixels;

  // -- Constructor --

  /** Constructs a new OME reader. */
  public OMEReader() { super("Open Microscopy Environment (OME)", "*"); }

  // -- Internal FormatReader API methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  protected void initFile(String id) throws FormatException, IOException {
    if (id.equals(loginString)) return;

    OMECredentials cred = OMEUtils.parseCredentials(id);
    id = String.valueOf(cred.imageID);

    super.initFile(id);

    // do sanity check on server name
    if (cred.server.startsWith("http:")) {
      cred.server = cred.server.substring(5);
    }
    while (cred.server.startsWith("/")) cred.server = cred.server.substring(1);
    int slash = cred.server.indexOf("/");
    if (slash >= 0) cred.server = cred.server.substring(0, slash);
    int colon = cred.server.indexOf(":");
    if (colon >= 0) cred.server = cred.server.substring(0, colon);

    currentId = cred.server + ":" + cred.imageID;

    String omeis = "http://" + cred.server + "/cgi-bin/omeis";
    cred.server = "http://" + cred.server + "/shoola/";
    cred.isOMERO = false;

    try {
      OMEUtils.login(cred);
    }
    catch (ReflectException e) {
      throw new FormatException(e);
    }

    String user = cred.username;
    String pass = cred.password;

    Criteria c = new Criteria();
    c.addWantedField("id");
    c.addWantedField("default_pixels");
    c.addWantedField("default_pixels", "PixelType");
    c.addWantedField("default_pixels", "SizeX");
    c.addWantedField("default_pixels", "SizeY");
    c.addWantedField("default_pixels", "SizeZ");
    c.addWantedField("default_pixels", "SizeC");
    c.addWantedField("default_pixels", "SizeT");
    c.addWantedField("default_pixels", "ImageServerID");
    c.addWantedField("default_pixels", "Repository");
    c.addWantedField("default_pixels.Repository", "ImageServerURL");
    c.addFilter("id", "=", String.valueOf(cred.imageID));

    FieldsSpecification fs = new FieldsSpecification();
    fs.addWantedField("Repository");
    fs.addWantedField("Repository", "ImageServerURL");
    c.addWantedFields("default_pixels", fs);

    rs = DataServer.getDefaultServices(cred.server);

    rc = rs.getRemoteCaller();

    if (user != null && pass != null) rc.login(user, pass);
    else if (sessionKey != null) rc.setSessionKey(sessionKey);

    df = (DataFactory) rs.getService(DataFactory.class);
    pf = (PixelsFactory) rs.getService(PixelsFactory.class);

    Image img = (Image) df.retrieveList(Image.class, c).get(0);
    pixels = img.getDefaultPixels();
    pixels.getRepository().setImageServerURL(omeis);

    try {
      thumb = pf.getThumbnail(pixels);
    }
    catch (ImageServerException exc) {
      if (debug) trace(exc);
    }

    core.sizeX[0] = pixels.getSizeX().intValue();
    core.sizeY[0] = pixels.getSizeY().intValue();
    core.sizeZ[0] = pixels.getSizeZ().intValue();
    core.sizeC[0] = pixels.getSizeC().intValue();
    core.sizeT[0] = pixels.getSizeT().intValue();
    core.pixelType[0] = FormatTools.pixelTypeFromString(pixels.getPixelType());
    core.currentOrder[0] = "XYZCT";

    core.imageCount[0] = core.sizeZ[0] * core.sizeC[0] * core.sizeT[0];
    core.rgb[0] = false;

    core.thumbSizeX[0] = thumb.getWidth();
    core.thumbSizeY[0] = thumb.getHeight();

    core.littleEndian[0] = true;
    core.interleaved[0] = false;

    MetadataStore store = getMetadataStore();
    store.setPixels(
      new Integer(core.sizeX[0]),
      new Integer(core.sizeY[0]),
      new Integer(core.sizeZ[0]),
      new Integer(core.sizeC[0]),
      new Integer(core.sizeT[0]),
      new Integer(core.pixelType[0]),
      new Boolean(!core.littleEndian[0]),
      core.currentOrder[0],
      null,
      null);
    for (int i=0; i<core.sizeC[0]; i++) {
      store.setLogicalChannel(i, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null,
        null, null, null, null, null);
    }
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isThisType(byte[]) */
  public boolean isThisType(byte[] block) {
    return true;
  }

  /* @see loci.formats.IFormatReader#openBytes(int, byte[]) */
  public byte[] openBytes(int no, byte[] buf)
    throws FormatException, IOException
  {
    FormatTools.assertId(currentId, true, 1);
    FormatTools.checkPlaneNumber(this, no);
    FormatTools.checkBufferSize(this, buf.length);
    int[] indices = getZCTCoords(no);
    try {
      buf = pf.getPlane(pixels, indices[0], indices[1], indices[2], false);
      return buf;
    }
    catch (ImageServerException e) {
      throw new FormatException(e);
    }
  }

  /* @see loci.formats.IFormatReader#openThumbBytes(int) */
  public byte[] openThumbBytes(int no) throws FormatException, IOException {
    FormatTools.checkPlaneNumber(this, no);
    byte[][] b = ImageTools.getPixelBytes(openThumbImage(no), true);
    byte[] rtn = new byte[b.length * b[0].length];
    for (int i=0; i<b.length; i++) {
      System.arraycopy(b[i], 0, rtn, i*b[0].length, b[i].length);
    }
    return rtn;
  }

  /* @see loci.formats.IFormatReader#openThumbImage(int) */
  public BufferedImage openThumbImage(int no)
    throws FormatException, IOException
  {
    FormatTools.assertId(currentId, true, 1);
    FormatTools.checkPlaneNumber(this, no);
    return thumb;
  }

  /* @see loci.formats.IFormatReader#close(boolean) */
  public void close(boolean fileOnly) throws IOException {
    if (fileOnly) {
      if (rc != null) rc.logout();
    }
    else close();
  }

  /* @see loci.formats.IFormatReader#close() */
  public void close() throws IOException {
    if (rc != null) rc.logout();
    thumb = null;
    rs = null;
    rc = null;
    df = null;
    pf = null;
    pixels = null;
    currentId = null;
    loginString = null;
  }

  // -- IFormatHandler API methods --

  /* @see loci.formats.IFormatHandler#isThisType(String) */
  public boolean isThisType(String id) {
    return id.indexOf("id") != -1 && (id.indexOf("password") != -1 ||
      id.indexOf("sessionKey") != -1);
  }

}
