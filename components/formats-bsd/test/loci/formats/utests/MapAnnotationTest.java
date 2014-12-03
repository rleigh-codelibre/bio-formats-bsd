/*
 * #%L
 * Tests for OME Bio-Formats BSD-licensed readers and writers.
 * %%
 * Copyright (C) 2005 - 2014 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package loci.formats.utests;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ome.xml.model.Image;
import ome.xml.model.MapAnnotation;
import ome.xml.model.MapPairs;
import ome.xml.model.OME;
import ome.xml.model.OMEModel;
import ome.xml.model.OMEModelImpl;
import ome.xml.model.Pixels;
import ome.xml.model.StructuredAnnotations;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test case for MapAnnotation values
 *
 *
 * @author Andrew Patterson
 */
public class MapAnnotationTest {

  private OME ome = new OME();

  @BeforeClass
  public void setUp() throws Exception {
    // Add an Image/Pixels
    Image image = new Image();
    image.setID("Image:0");
    Pixels pixels = new Pixels();
    pixels.setID("Pixels:0");
    image.setPixels(pixels);

    // Add a Map Annotation
    MapAnnotation mapAnnotation = new MapAnnotation();
    mapAnnotation.setID("Annotation:0");
    mapAnnotation.setValue(new MapPairs());
    mapAnnotation.getValue().getMap().put("a","1");
    mapAnnotation.getValue().getMap().put("b","2");
    mapAnnotation.getValue().getMap().put("c","3");
    mapAnnotation.getValue().getMap().put("d","4");
    mapAnnotation.getValue().getMap().put("e","5");

    StructuredAnnotations structuredAnnotations = new StructuredAnnotations();
    structuredAnnotations.addMapAnnotation(mapAnnotation);
    ome.setStructuredAnnotations(structuredAnnotations );
    image.linkAnnotation(mapAnnotation);
    ome.addImage(image);
  }

  @Test
  public void testMapAnnotationValid() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = factory.newDocumentBuilder();
    Document document = parser.newDocument();
    // Produce a valid OME DOM element hierarchy
    Element root = ome.asXMLElement(document);
    SPWModelMock.postProcess(root, document, false);
    OMEModel model = new OMEModelImpl();
    ome = new OME(document.getDocumentElement(), model);
    model.resolveReferences();
  }

  @Test
  public void testMapAnnotationValueContent() throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = factory.newDocumentBuilder();
    Document document = parser.newDocument();
    // Produce a valid OME DOM element hierarchy
    Element root = ome.asXMLElement(document);
    SPWModelMock.postProcess(root, document, false);
    OMEModel model = new OMEModelImpl();
    ome = new OME(document.getDocumentElement(), model);
    model.resolveReferences();

    assertNotNull(ome); 
    assertEquals(ome.getImage(0).getPixels().getID(), "Pixels:0"); 
    assertNotNull(ome.getImage(0).getLinkedAnnotation(0)); 

    MapAnnotation mapAnnotation = (MapAnnotation) ome.getImage(0).getLinkedAnnotation(0); 
    MapPairs dataMap = mapAnnotation.getValue();

    assertTrue(dataMap.getMap().containsKey("a"));
    assertTrue(dataMap.getMap().containsKey("b"));
    assertTrue(dataMap.getMap().containsKey("c"));
    assertTrue(dataMap.getMap().containsKey("d"));
    assertTrue(dataMap.getMap().containsKey("e"));

    assertFalse(dataMap.getMap().containsKey("x"));

    assertTrue(dataMap.getMap().containsValue("1"));
    assertTrue(dataMap.getMap().containsValue("2"));
    assertTrue(dataMap.getMap().containsValue("3"));
    assertTrue(dataMap.getMap().containsValue("4"));
    assertTrue(dataMap.getMap().containsValue("5"));

    assertFalse(dataMap.getMap().containsValue("-1"));

    assertEquals(dataMap.getMap().get("a"),"1");
    assertEquals(dataMap.getMap().get("b"),"2");
    assertEquals(dataMap.getMap().get("c"),"3");
    assertEquals(dataMap.getMap().get("d"),"4");
    assertEquals(dataMap.getMap().get("e"),"5");
  }

}
