//
// MetadataStore.java
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

/*-----------------------------------------------------------------------------
 *
 * THIS IS AUTOMATICALLY GENERATED CODE.  DO NOT MODIFY.
 * Created by curtis via MetadataAutogen on Apr 24, 2008 4:44:24 PM CDT
 *
 *-----------------------------------------------------------------------------
 */

package loci.formats.meta;

/**
 * A proxy whose responsibility it is to marshal biological image data into a
 * particular storage medium.
 *
 * The <code>MetadataStore</code> interface encompasses the basic metadata that
 * any specific storage medium (file, relational database, etc.) should be
 * expected to store into its backing data model.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/loci/formats/meta/MetadataStore.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/loci/formats/meta/MetadataStore.java">SVN</a></dd></dl>
 *
 * @author Chris Allan callan at blackcat.ca
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public interface MetadataStore {

  void createRoot();

  Object getRoot();

  void setRoot(Object root);

  // - Arc property storage -

  /**
   * For a particular Arc, sets TODO.
   * @param type TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setArcType(String type, int instrumentIndex, int lightSourceIndex);

  // - ChannelComponent property storage -

  /**
   * For a particular ChannelComponent, sets which color channel this ChannelComponent belongs to (for example, 'R' for an 'RGB' PhotometricInterpretation).
   * @param colorDomain which color channel this ChannelComponent belongs to (for example, 'R' for an 'RGB' PhotometricInterpretation)
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   * @param channelComponentIndex index of the ChannelComponent
   */
  void setChannelComponentColorDomain(String colorDomain, int imageIndex, int logicalChannelIndex, int channelComponentIndex);

  /**
   * For a particular ChannelComponent, sets the index into the channel dimension of the 5-D pixel array.
   * @param index the index into the channel dimension of the 5-D pixel array
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   * @param channelComponentIndex index of the ChannelComponent
   */
  void setChannelComponentIndex(Integer index, int imageIndex, int logicalChannelIndex, int channelComponentIndex);

  // - Detector property storage -

  /**
   * For a particular Detector, sets TODO.
   * @param gain TODO
   * @param instrumentIndex index of the Instrument
   * @param detectorIndex index of the Detector
   */
  void setDetectorGain(Float gain, int instrumentIndex, int detectorIndex);

  /**
   * For a particular Detector, sets TODO.
   * @param id TODO
   * @param instrumentIndex index of the Instrument
   * @param detectorIndex index of the Detector
   */
  void setDetectorID(String id, int instrumentIndex, int detectorIndex);

  /**
   * For a particular Detector, sets TODO.
   * @param manufacturer TODO
   * @param instrumentIndex index of the Instrument
   * @param detectorIndex index of the Detector
   */
  void setDetectorManufacturer(String manufacturer, int instrumentIndex, int detectorIndex);

  /**
   * For a particular Detector, sets TODO.
   * @param model TODO
   * @param instrumentIndex index of the Instrument
   * @param detectorIndex index of the Detector
   */
  void setDetectorModel(String model, int instrumentIndex, int detectorIndex);

  /**
   * For a particular Detector, sets TODO.
   * @param offset TODO
   * @param instrumentIndex index of the Instrument
   * @param detectorIndex index of the Detector
   */
  void setDetectorOffset(Float offset, int instrumentIndex, int detectorIndex);

  /**
   * For a particular Detector, sets TODO.
   * @param serialNumber TODO
   * @param instrumentIndex index of the Instrument
   * @param detectorIndex index of the Detector
   */
  void setDetectorSerialNumber(String serialNumber, int instrumentIndex, int detectorIndex);

  /**
   * For a particular Detector, sets TODO.
   * @param type TODO
   * @param instrumentIndex index of the Instrument
   * @param detectorIndex index of the Detector
   */
  void setDetectorType(String type, int instrumentIndex, int detectorIndex);

  /**
   * For a particular Detector, sets TODO.
   * @param voltage TODO
   * @param instrumentIndex index of the Instrument
   * @param detectorIndex index of the Detector
   */
  void setDetectorVoltage(Float voltage, int instrumentIndex, int detectorIndex);

  // - DetectorSettings property storage -

  /**
   * For a particular DetectorSettings, sets the detector associated with this channel.
   * @param detector the detector associated with this channel
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setDetectorSettingsDetector(String detector, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular DetectorSettings, sets the detector gain.
   * @param gain the detector gain
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setDetectorSettingsGain(Float gain, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular DetectorSettings, sets the detector offset.
   * @param offset the detector offset
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setDetectorSettingsOffset(Float offset, int imageIndex, int logicalChannelIndex);

  // - Dimensions property storage -

  /**
   * For a particular Dimensions, sets the size of an individual pixel's X axis in microns.
   * @param physicalSizeX the size of an individual pixel's X axis in microns
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setDimensionsPhysicalSizeX(Float physicalSizeX, int imageIndex, int pixelsIndex);

  /**
   * For a particular Dimensions, sets the size of an individual pixel's Y axis in microns.
   * @param physicalSizeY the size of an individual pixel's Y axis in microns
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setDimensionsPhysicalSizeY(Float physicalSizeY, int imageIndex, int pixelsIndex);

  /**
   * For a particular Dimensions, sets the size of an individual pixel's Z axis in microns.
   * @param physicalSizeZ the size of an individual pixel's Z axis in microns
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setDimensionsPhysicalSizeZ(Float physicalSizeZ, int imageIndex, int pixelsIndex);

  /**
   * For a particular Dimensions, sets the distance between adjacent time points in seconds.
   * @param timeIncrement the distance between adjacent time points in seconds
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setDimensionsTimeIncrement(Float timeIncrement, int imageIndex, int pixelsIndex);

  /**
   * For a particular Dimensions, sets the distance between adjacent wavelengths in nanometers.
   * @param waveIncrement the distance between adjacent wavelengths in nanometers
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setDimensionsWaveIncrement(Integer waveIncrement, int imageIndex, int pixelsIndex);

  /**
   * For a particular Dimensions, sets the starting wavelength in nanometers.
   * @param waveStart the starting wavelength in nanometers
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setDimensionsWaveStart(Integer waveStart, int imageIndex, int pixelsIndex);

  // - DisplayOptions property storage -

  /**
   * For a particular DisplayOptions, sets TODO.
   * @param id TODO
   * @param imageIndex index of the Image
   */
  void setDisplayOptionsID(String id, int imageIndex);

  /**
   * For a particular DisplayOptions, sets zoom factor for use in the display (NOT THE LENS ZOOM).
   * @param zoom zoom factor for use in the display (NOT THE LENS ZOOM)
   * @param imageIndex index of the Image
   */
  void setDisplayOptionsZoom(Float zoom, int imageIndex);

  // - DisplayOptionsProjection property storage -

  /**
   * For a particular DisplayOptionsProjection, sets the first focal plane to include in the maximum intensity projection.
   * @param zStart the first focal plane to include in the maximum intensity projection
   * @param imageIndex index of the Image
   */
  void setDisplayOptionsProjectionZStart(Integer zStart, int imageIndex);

  /**
   * For a particular DisplayOptionsProjection, sets the last focal plane to include in the maximum intensity projection.
   * @param zStop the last focal plane to include in the maximum intensity projection
   * @param imageIndex index of the Image
   */
  void setDisplayOptionsProjectionZStop(Integer zStop, int imageIndex);

  // - DisplayOptionsTime property storage -

  /**
   * For a particular DisplayOptionsTime, sets the first time point to include in the animation.
   * @param tStart the first time point to include in the animation
   * @param imageIndex index of the Image
   */
  void setDisplayOptionsTimeTStart(Integer tStart, int imageIndex);

  /**
   * For a particular DisplayOptionsTime, sets the last time point to include in the animation.
   * @param tStop the last time point to include in the animation
   * @param imageIndex index of the Image
   */
  void setDisplayOptionsTimeTStop(Integer tStop, int imageIndex);

  // - Experimenter property storage -

  /**
   * For a particular Experimenter, sets the e-mail address of the experimenter.
   * @param email the e-mail address of the experimenter
   * @param experimenterIndex index of the Experimenter
   */
  void setExperimenterEmail(String email, int experimenterIndex);

  /**
   * For a particular Experimenter, sets the first name of the experimenter.
   * @param firstName the first name of the experimenter
   * @param experimenterIndex index of the Experimenter
   */
  void setExperimenterFirstName(String firstName, int experimenterIndex);

  /**
   * For a particular Experimenter, sets TODO.
   * @param id TODO
   * @param experimenterIndex index of the Experimenter
   */
  void setExperimenterID(String id, int experimenterIndex);

  /**
   * For a particular Experimenter, sets the institution to which the experimenter belongs.
   * @param institution the institution to which the experimenter belongs
   * @param experimenterIndex index of the Experimenter
   */
  void setExperimenterInstitution(String institution, int experimenterIndex);

  /**
   * For a particular Experimenter, sets the last name of the experimenter.
   * @param lastName the last name of the experimenter
   * @param experimenterIndex index of the Experimenter
   */
  void setExperimenterLastName(String lastName, int experimenterIndex);

  // - Filament property storage -

  /**
   * For a particular Filament, sets TODO.
   * @param type TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setFilamentType(String type, int instrumentIndex, int lightSourceIndex);

  // - Image property storage -

  /**
   * For a particular Image, sets the creation date of the image.
   * @param creationDate the creation date of the image
   * @param imageIndex index of the Image
   */
  void setImageCreationDate(String creationDate, int imageIndex);

  /**
   * For a particular Image, sets the full description of the image.
   * @param description the full description of the image
   * @param imageIndex index of the Image
   */
  void setImageDescription(String description, int imageIndex);

  /**
   * For a particular Image, sets TODO.
   * @param id TODO
   * @param imageIndex index of the Image
   */
  void setImageID(String id, int imageIndex);

  /**
   * For a particular Image, sets the full name of the image.
   * @param name the full name of the image
   * @param imageIndex index of the Image
   */
  void setImageName(String name, int imageIndex);

  // - ImagingEnvironment property storage -

  /**
   * For a particular ImagingEnvironment, sets TODO.
   * @param airPressure TODO
   * @param imageIndex index of the Image
   */
  void setImagingEnvironmentAirPressure(Float airPressure, int imageIndex);

  /**
   * For a particular ImagingEnvironment, sets TODO.
   * @param cO2Percent TODO
   * @param imageIndex index of the Image
   */
  void setImagingEnvironmentCO2Percent(Float cO2Percent, int imageIndex);

  /**
   * For a particular ImagingEnvironment, sets TODO.
   * @param humidity TODO
   * @param imageIndex index of the Image
   */
  void setImagingEnvironmentHumidity(Float humidity, int imageIndex);

  /**
   * For a particular ImagingEnvironment, sets TODO.
   * @param temperature TODO
   * @param imageIndex index of the Image
   */
  void setImagingEnvironmentTemperature(Float temperature, int imageIndex);

  // - Instrument property storage -

  /**
   * For a particular Instrument, sets TODO.
   * @param id TODO
   * @param instrumentIndex index of the Instrument
   */
  void setInstrumentID(String id, int instrumentIndex);

  // - Laser property storage -

  /**
   * For a particular Laser, sets TODO.
   * @param frequencyMultiplication TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLaserFrequencyMultiplication(Integer frequencyMultiplication, int instrumentIndex, int lightSourceIndex);

  /**
   * For a particular Laser, sets TODO.
   * @param laserMedium TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLaserLaserMedium(String laserMedium, int instrumentIndex, int lightSourceIndex);

  /**
   * For a particular Laser, sets TODO.
   * @param pulse TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLaserPulse(String pulse, int instrumentIndex, int lightSourceIndex);

  /**
   * For a particular Laser, sets TODO.
   * @param tuneable TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLaserTuneable(Boolean tuneable, int instrumentIndex, int lightSourceIndex);

  /**
   * For a particular Laser, sets TODO.
   * @param type TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLaserType(String type, int instrumentIndex, int lightSourceIndex);

  /**
   * For a particular Laser, sets TODO.
   * @param wavelength TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLaserWavelength(Integer wavelength, int instrumentIndex, int lightSourceIndex);

  // - LightSource property storage -

  /**
   * For a particular LightSource, sets TODO.
   * @param id TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLightSourceID(String id, int instrumentIndex, int lightSourceIndex);

  /**
   * For a particular LightSource, sets TODO.
   * @param manufacturer TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLightSourceManufacturer(String manufacturer, int instrumentIndex, int lightSourceIndex);

  /**
   * For a particular LightSource, sets TODO.
   * @param model TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLightSourceModel(String model, int instrumentIndex, int lightSourceIndex);

  /**
   * For a particular LightSource, sets TODO.
   * @param power TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLightSourcePower(Float power, int instrumentIndex, int lightSourceIndex);

  /**
   * For a particular LightSource, sets TODO.
   * @param serialNumber TODO
   * @param instrumentIndex index of the Instrument
   * @param lightSourceIndex index of the LightSource
   */
  void setLightSourceSerialNumber(String serialNumber, int instrumentIndex, int lightSourceIndex);

  // - LightSourceSettings property storage -

  /**
   * For a particular LightSourceSettings, sets the primary light source attenuation.
   * @param attenuation the primary light source attenuation
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLightSourceSettingsAttenuation(Float attenuation, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LightSourceSettings, sets the primary light source.
   * @param lightSource the primary light source
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLightSourceSettingsLightSource(String lightSource, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LightSourceSettings, sets the primary light source wavelength.
   * @param wavelength the primary light source wavelength
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLightSourceSettingsWavelength(Integer wavelength, int imageIndex, int logicalChannelIndex);

  // - LogicalChannel property storage -

  /**
   * For a particular LogicalChannel, sets the constrast method name.
   * @param contrastMethod the constrast method name
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelContrastMethod(String contrastMethod, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets the emission wavelength.
   * @param emWave the emission wavelength
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelEmWave(Integer emWave, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets the excitation wavelength.
   * @param exWave the excitation wavelength
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelExWave(Integer exWave, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets the fluorescence type.
   * @param fluor the fluorescence type
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelFluor(String fluor, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets TODO.
   * @param id TODO
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelID(String id, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets the illumination type.
   * @param illuminationType the illumination type
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelIlluminationType(String illuminationType, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets the acquisition mode.
   * @param mode the acquisition mode
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelMode(String mode, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets the logical channel's name.
   * @param name the logical channel's name
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelName(String name, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets the neutral-density filter value.
   * @param ndFilter the neutral-density filter value
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelNdFilter(Float ndFilter, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets the photometric interpretation type.
   * @param photometricInterpretation the photometric interpretation type
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelPhotometricInterpretation(String photometricInterpretation, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets the size of the pinhole.
   * @param pinholeSize the size of the pinhole
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelPinholeSize(Integer pinholeSize, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets TODO.
   * @param pockelCellSetting TODO
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelPockelCellSetting(Integer pockelCellSetting, int imageIndex, int logicalChannelIndex);

  /**
   * For a particular LogicalChannel, sets TODO.
   * @param samplesPerPixel TODO
   * @param imageIndex index of the Image
   * @param logicalChannelIndex index of the LogicalChannel
   */
  void setLogicalChannelSamplesPerPixel(Integer samplesPerPixel, int imageIndex, int logicalChannelIndex);

  // - OTF property storage -

  /**
   * For a particular OTF, sets TODO.
   * @param id TODO
   * @param instrumentIndex index of the Instrument
   * @param otfIndex index of the OTF
   */
  void setOTFID(String id, int instrumentIndex, int otfIndex);

  /**
   * For a particular OTF, sets TODO.
   * @param opticalAxisAveraged TODO
   * @param instrumentIndex index of the Instrument
   * @param otfIndex index of the OTF
   */
  void setOTFOpticalAxisAveraged(Boolean opticalAxisAveraged, int instrumentIndex, int otfIndex);

  /**
   * For a particular OTF, sets TODO.
   * @param pixelType TODO
   * @param instrumentIndex index of the Instrument
   * @param otfIndex index of the OTF
   */
  void setOTFPixelType(String pixelType, int instrumentIndex, int otfIndex);

  /**
   * For a particular OTF, sets TODO.
   * @param sizeX TODO
   * @param instrumentIndex index of the Instrument
   * @param otfIndex index of the OTF
   */
  void setOTFSizeX(Integer sizeX, int instrumentIndex, int otfIndex);

  /**
   * For a particular OTF, sets TODO.
   * @param sizeY TODO
   * @param instrumentIndex index of the Instrument
   * @param otfIndex index of the OTF
   */
  void setOTFSizeY(Integer sizeY, int instrumentIndex, int otfIndex);

  // - Objective property storage -

  /**
   * For a particular Objective, sets TODO.
   * @param calibratedMagnification TODO
   * @param instrumentIndex index of the Instrument
   * @param objectiveIndex index of the Objective
   */
  void setObjectiveCalibratedMagnification(Float calibratedMagnification, int instrumentIndex, int objectiveIndex);

  /**
   * For a particular Objective, sets TODO.
   * @param correction TODO
   * @param instrumentIndex index of the Instrument
   * @param objectiveIndex index of the Objective
   */
  void setObjectiveCorrection(String correction, int instrumentIndex, int objectiveIndex);

  /**
   * For a particular Objective, sets TODO.
   * @param id TODO
   * @param instrumentIndex index of the Instrument
   * @param objectiveIndex index of the Objective
   */
  void setObjectiveID(String id, int instrumentIndex, int objectiveIndex);

  /**
   * For a particular Objective, sets TODO.
   * @param immersion TODO
   * @param instrumentIndex index of the Instrument
   * @param objectiveIndex index of the Objective
   */
  void setObjectiveImmersion(String immersion, int instrumentIndex, int objectiveIndex);

  /**
   * For a particular Objective, sets TODO.
   * @param lensNA TODO
   * @param instrumentIndex index of the Instrument
   * @param objectiveIndex index of the Objective
   */
  void setObjectiveLensNA(Float lensNA, int instrumentIndex, int objectiveIndex);

  /**
   * For a particular Objective, sets TODO.
   * @param manufacturer TODO
   * @param instrumentIndex index of the Instrument
   * @param objectiveIndex index of the Objective
   */
  void setObjectiveManufacturer(String manufacturer, int instrumentIndex, int objectiveIndex);

  /**
   * For a particular Objective, sets TODO.
   * @param model TODO
   * @param instrumentIndex index of the Instrument
   * @param objectiveIndex index of the Objective
   */
  void setObjectiveModel(String model, int instrumentIndex, int objectiveIndex);

  /**
   * For a particular Objective, sets TODO.
   * @param nominalMagnification TODO
   * @param instrumentIndex index of the Instrument
   * @param objectiveIndex index of the Objective
   */
  void setObjectiveNominalMagnification(Integer nominalMagnification, int instrumentIndex, int objectiveIndex);

  /**
   * For a particular Objective, sets TODO.
   * @param serialNumber TODO
   * @param instrumentIndex index of the Instrument
   * @param objectiveIndex index of the Objective
   */
  void setObjectiveSerialNumber(String serialNumber, int instrumentIndex, int objectiveIndex);

  /**
   * For a particular Objective, sets TODO.
   * @param workingDistance TODO
   * @param instrumentIndex index of the Instrument
   * @param objectiveIndex index of the Objective
   */
  void setObjectiveWorkingDistance(Float workingDistance, int instrumentIndex, int objectiveIndex);

  // - Pixels property storage -

  /**
   * For a particular Pixels, sets endianness of the pixels set.
   * @param bigEndian endianness of the pixels set
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setPixelsBigEndian(Boolean bigEndian, int imageIndex, int pixelsIndex);

  /**
   * For a particular Pixels, sets the dimension order of the pixels set.
   * @param dimensionOrder the dimension order of the pixels set
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setPixelsDimensionOrder(String dimensionOrder, int imageIndex, int pixelsIndex);

  /**
   * For a particular Pixels, sets TODO.
   * @param id TODO
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setPixelsID(String id, int imageIndex, int pixelsIndex);

  /**
   * For a particular Pixels, sets the pixel type.
   * @param pixelType the pixel type
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setPixelsPixelType(String pixelType, int imageIndex, int pixelsIndex);

  /**
   * For a particular Pixels, sets number of channels per timepoint.
   * @param sizeC number of channels per timepoint
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setPixelsSizeC(Integer sizeC, int imageIndex, int pixelsIndex);

  /**
   * For a particular Pixels, sets number of timepoints.
   * @param sizeT number of timepoints
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setPixelsSizeT(Integer sizeT, int imageIndex, int pixelsIndex);

  /**
   * For a particular Pixels, sets The size of an individual plane or section's X axis (width)..
   * @param sizeX The size of an individual plane or section's X axis (width).
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setPixelsSizeX(Integer sizeX, int imageIndex, int pixelsIndex);

  /**
   * For a particular Pixels, sets The size of an individual plane or section's Y axis (height)..
   * @param sizeY The size of an individual plane or section's Y axis (height).
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setPixelsSizeY(Integer sizeY, int imageIndex, int pixelsIndex);

  /**
   * For a particular Pixels, sets number of optical sections per stack.
   * @param sizeZ number of optical sections per stack
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   */
  void setPixelsSizeZ(Integer sizeZ, int imageIndex, int pixelsIndex);

  // - Plane property storage -

  /**
   * For a particular Plane, sets the channel index.
   * @param theC the channel index
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   * @param planeIndex index of the Plane
   */
  void setPlaneTheC(Integer theC, int imageIndex, int pixelsIndex, int planeIndex);

  /**
   * For a particular Plane, sets the timepoint.
   * @param theT the timepoint
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   * @param planeIndex index of the Plane
   */
  void setPlaneTheT(Integer theT, int imageIndex, int pixelsIndex, int planeIndex);

  /**
   * For a particular Plane, sets the optical section index.
   * @param theZ the optical section index
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   * @param planeIndex index of the Plane
   */
  void setPlaneTheZ(Integer theZ, int imageIndex, int pixelsIndex, int planeIndex);

  // - PlaneTiming property storage -

  /**
   * For a particular PlaneTiming, sets the time in seconds since the beginning of the experiment.
   * @param deltaT the time in seconds since the beginning of the experiment
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   * @param planeIndex index of the Plane
   */
  void setPlaneTimingDeltaT(Float deltaT, int imageIndex, int pixelsIndex, int planeIndex);

  /**
   * For a particular PlaneTiming, sets the exposure time in seconds.
   * @param exposureTime the exposure time in seconds
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   * @param planeIndex index of the Plane
   */
  void setPlaneTimingExposureTime(Float exposureTime, int imageIndex, int pixelsIndex, int planeIndex);

  // - ROI property storage -

  /**
   * For a particular ROI, sets TODO.
   * @param id TODO
   * @param imageIndex index of the Image
   * @param roiIndex index of the ROI
   */
  void setROIID(String id, int imageIndex, int roiIndex);

  /**
   * For a particular ROI, sets the starting timepoint.
   * @param t0 the starting timepoint
   * @param imageIndex index of the Image
   * @param roiIndex index of the ROI
   */
  void setROIT0(Integer t0, int imageIndex, int roiIndex);

  /**
   * For a particular ROI, sets the ending timepoint.
   * @param t1 the ending timepoint
   * @param imageIndex index of the Image
   * @param roiIndex index of the ROI
   */
  void setROIT1(Integer t1, int imageIndex, int roiIndex);

  /**
   * For a particular ROI, sets the starting X coordinate.
   * @param x0 the starting X coordinate
   * @param imageIndex index of the Image
   * @param roiIndex index of the ROI
   */
  void setROIX0(Integer x0, int imageIndex, int roiIndex);

  /**
   * For a particular ROI, sets the ending X coordinate.
   * @param x1 the ending X coordinate
   * @param imageIndex index of the Image
   * @param roiIndex index of the ROI
   */
  void setROIX1(Integer x1, int imageIndex, int roiIndex);

  /**
   * For a particular ROI, sets the starting Y coordinate.
   * @param y0 the starting Y coordinate
   * @param imageIndex index of the Image
   * @param roiIndex index of the ROI
   */
  void setROIY0(Integer y0, int imageIndex, int roiIndex);

  /**
   * For a particular ROI, sets the ending Y coordinate.
   * @param y1 the ending Y coordinate
   * @param imageIndex index of the Image
   * @param roiIndex index of the ROI
   */
  void setROIY1(Integer y1, int imageIndex, int roiIndex);

  /**
   * For a particular ROI, sets the starting Z coordinate.
   * @param z0 the starting Z coordinate
   * @param imageIndex index of the Image
   * @param roiIndex index of the ROI
   */
  void setROIZ0(Integer z0, int imageIndex, int roiIndex);

  /**
   * For a particular ROI, sets the ending Z coordinate.
   * @param z1 the ending Z coordinate
   * @param imageIndex index of the Image
   * @param roiIndex index of the ROI
   */
  void setROIZ1(Integer z1, int imageIndex, int roiIndex);

  // - StageLabel property storage -

  /**
   * For a particular StageLabel, sets a name for the stage label.
   * @param name a name for the stage label
   * @param imageIndex index of the Image
   */
  void setStageLabelName(String name, int imageIndex);

  /**
   * For a particular StageLabel, sets the x coordinate of the stage.
   * @param x the x coordinate of the stage
   * @param imageIndex index of the Image
   */
  void setStageLabelX(Float x, int imageIndex);

  /**
   * For a particular StageLabel, sets the y coordinate of the stage.
   * @param y the y coordinate of the stage
   * @param imageIndex index of the Image
   */
  void setStageLabelY(Float y, int imageIndex);

  /**
   * For a particular StageLabel, sets the z coordinate of the stage.
   * @param z the z coordinate of the stage
   * @param imageIndex index of the Image
   */
  void setStageLabelZ(Float z, int imageIndex);

  // - StagePosition property storage -

  /**
   * For a particular StagePosition, sets the X coordinate of the stage position.
   * @param positionX the X coordinate of the stage position
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   * @param planeIndex index of the Plane
   */
  void setStagePositionPositionX(Float positionX, int imageIndex, int pixelsIndex, int planeIndex);

  /**
   * For a particular StagePosition, sets the Y coordinate of the stage position.
   * @param positionY the Y coordinate of the stage position
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   * @param planeIndex index of the Plane
   */
  void setStagePositionPositionY(Float positionY, int imageIndex, int pixelsIndex, int planeIndex);

  /**
   * For a particular StagePosition, sets the Z coordinate of the stage position.
   * @param positionZ the Z coordinate of the stage position
   * @param imageIndex index of the Image
   * @param pixelsIndex index of the Pixels
   * @param planeIndex index of the Plane
   */
  void setStagePositionPositionZ(Float positionZ, int imageIndex, int pixelsIndex, int planeIndex);

}
