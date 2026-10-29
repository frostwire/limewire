package com.t4l.jmf;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.Buffer;
import javax.media.Codec;
import javax.media.Format;
import javax.media.PlugIn;
import javax.media.ResourceUnavailableException;
import javax.media.format.JPEGFormat;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

import net.sf.fmj.utility.LoggerSingleton;

/** 
 * This type of MOV file just stores frames as a series of JPEG
 * images.
 * This codec uses ImageIO classes to convert from the jpeg VideoFormat
 * to RGBFormats.
 * @author Jeremy Wood
 */
public class JPEGDecoder implements Codec {
	private static final Logger logger = LoggerSingleton.logger;

	private static final JPEGFormat jpegFormat = new JPEGFormat();
	private static final RGBFormat rgbFormat = new RGBFormat(null, -1, Format.intArray, -1.f, -1, -1, -1, -1);
	

	public Format[] getSupportedInputFormats() {
		return new VideoFormat[] { jpegFormat};
	}

	public Format[] getSupportedOutputFormats(Format input) {
		if (input == null)
			return new VideoFormat[] { rgbFormat };
		
		if(input.relax().matches(jpegFormat)) {
			final VideoFormat inputVideoFormat = (VideoFormat) input;
			// TODO: 
			return new VideoFormat[] { new RGBFormat(inputVideoFormat.getSize(), -1, Format.intArray, inputVideoFormat.getFrameRate(), 32, 0xff0000, 0xff00, 0xff)};
		} 
		return new Format[] {};
	}

	public int process(Buffer input, Buffer output) {
		Format inputFormat = input.getFormat();
		Format outputFormat = output.getFormat();
		
		if(inputFormat.relax().matches(jpegFormat) && 
				(outputFormat==null || outputFormat.relax().matches(rgbFormat))) {
			return processJPEGtoRGB(input,output);
		} 
		return PlugIn.BUFFER_PROCESSED_FAILED;
	}

	
	static Hashtable imageTable = new Hashtable();
	protected int processJPEGtoRGB(Buffer input,Buffer output) {
		synchronized(imageTable) {
			try {
				VideoFormat inputFormat = (VideoFormat)input.getFormat();
				RGBFormat outputFormat = (RGBFormat)output.getFormat();
				
				if(outputFormat==null) {
					int width = inputFormat.getSize().width;
					int height = inputFormat.getSize().height;
					outputFormat = new RGBFormat(
							new Dimension(width,height),
							width*height,
							Format.intArray,
							inputFormat.getFrameRate(), 32,
							0xff0000, 0xff00, 0xff, //RGB masks
							1, width, //pixel stride, line stride
							RGBFormat.FALSE, RGBFormat.LITTLE_ENDIAN);
					output.setFormat(outputFormat);
				}
				
				
				byte[] b = (byte[])input.getData();
				Dimension d = inputFormat.getSize();
				BufferedImage dest = (BufferedImage)imageTable.get(d);
				if(dest==null) {
					dest = new BufferedImage(d.width,d.height,BufferedImage.TYPE_INT_RGB);
				}
				readJPEG(b, dest);
				
				imageTable.put(d, dest);
				
				Object obj = output.getData();
				int[] intArray;
				if(obj instanceof int[]) {
					intArray = (int[])obj;
				} else {
					intArray = new int[dest.getWidth()*dest.getHeight()];
					output.setData(intArray);
				}
				
				RGBConverter.populateArray(dest, intArray, (RGBFormat)output.getFormat());
				
				output.setDiscard(input.isDiscard());
				output.setDuration(input.getDuration());
				output.setEOM(input.isEOM());
				output.setFlags(input.getFlags()); //is this correct?
				output.setHeader(null);
				output.setTimeStamp(input.getTimeStamp());
				output.setSequenceNumber(input.getSequenceNumber());
				output.setOffset(0);
				output.setLength(dest.getWidth()*dest.getHeight());
				return PlugIn.BUFFER_PROCESSED_OK;
			} catch(Throwable t) {
				logger.log(Level.WARNING, "" + t, t);
				return PlugIn.BUFFER_PROCESSED_FAILED;
			}
		}
	}
	
	protected static void readJPEG(byte[] data,BufferedImage dest) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ImageInputStream stream = ImageIO.createImageInputStream(in);
		Iterator iter = ImageIO.getImageReaders(stream);
		ImageReader reader = (ImageReader)iter.next();
		if(reader==null)
			throw new UnsupportedOperationException("This image is unsupported.");
		reader.setInput(stream,false);
		
		ImageReadParam param = reader.getDefaultReadParam();
		param.setDestination(dest);
		reader.read(0,param);
	}
	
	
	public Format setInputFormat(Format f) {
		//we don't do anything here; just return positive feedback that we
		//can in fact handle whatever it gives us
		if(f.relax().matches(jpegFormat)) {
			return f;
		} 
		return null;
	}

	public Format setOutputFormat(Format f) {
		//we don't do anything here; just return positive feedback that we
		//can in fact handle whatever it gives us
		if(f.relax().matches(rgbFormat)) {
			return f;
		} 
		return null;
	}

	public void close() {
		synchronized(imageTable) {
			imageTable.clear();
		}
	}

	public String getName() {
		return "JPEG Decoder";
	}

	public void open() throws ResourceUnavailableException {}

	public void reset() {}

	public Object getControl(String controlType) {
		return null;
	}

	public Object[] getControls() {
		return new String[] {};
	}
}
