package net.sf.fmj.media.codec.audio.gsm;

import java.util.logging.Logger;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;

import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.media.AudioFormatCompleter;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.LoggingStringUtils;

/**
 * GSM to PCM java decoder.
 * Decodes GSM frame (33 bytes long) into 160 16-bit PCM samples (320 bytes).
 *
 * @author Martin Harvan
 */
public class Decoder
        extends AbstractCodec {
    private static final Logger logger = LoggerSingleton.logger;
    private Buffer innerBuffer = new Buffer();
    private static final int PCM_BYTES = 320;
    private static final int GSM_BYTES = 33;
    private int innerDataLength = 0;
    private int inputDataLength = 0;
    byte[] innerContent;

    @Override
    public String getName() {
        return "GSM Decoder";
    }

    public Decoder() {
        super();
        this.inputFormats = new Format[]{
                new AudioFormat(AudioFormat.GSM, 8000, 8, 1, -1, AudioFormat.SIGNED, 264, -1.0, Format.byteArray)
                
        };


    }

    // TODO: move to base class?
    protected Format[] outputFormats = new Format[]{
            new AudioFormat(AudioFormat.LINEAR, 8000, 16, 1, -1, AudioFormat.SIGNED, -1, -1.0, Format.byteArray)
    };

    @Override
    public Format setOutputFormat(Format format) {
        if (!(format instanceof AudioFormat))
            return null;
        final AudioFormat audioFormat = (AudioFormat) format;
        return super.setOutputFormat(AudioFormatCompleter.complete(audioFormat));
    }

    @Override
    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null)
            return outputFormats;
        else {
            if (!(input instanceof AudioFormat)) {
                logger.warning(this.getClass().getSimpleName() + ".getSupportedOutputFormats: input format does not match, returning format array of {null} for " + input); // this can cause an NPE in JMF if it ever happens.
                return new Format[]{null};
            }
            final AudioFormat inputCast = (AudioFormat) input;
            if (!inputCast.getEncoding().equals(AudioFormat.GSM) ||
                (inputCast.getSampleSizeInBits() != 8 && inputCast.getSampleSizeInBits() != Format.NOT_SPECIFIED) ||
                (inputCast.getChannels() != 1 && inputCast.getChannels() != Format.NOT_SPECIFIED) ||
                (inputCast.getSigned() != AudioFormat.SIGNED && inputCast.getSigned() != Format.NOT_SPECIFIED) ||
                (inputCast.getFrameSizeInBits() != 264 && inputCast.getFrameSizeInBits() != Format.NOT_SPECIFIED) ||
                (inputCast.getDataType() != null && inputCast.getDataType() != Format.byteArray)
                )
            {
                logger.warning(this.getClass().getSimpleName() + ".getSupportedOutputFormats: input format does not match, returning format array of {null} for " + input); // this can cause an NPE in JMF if it ever happens.
                return new Format[] {null};
            }

            final AudioFormat result = new AudioFormat(AudioFormat.LINEAR, inputCast.getSampleRate(), 16,
                    1, inputCast.getEndian(), AudioFormat.SIGNED, 16,
                    -1, Format.byteArray);

            return new Format[]{result};
        }
    }

    @Override
    public void open() {

    }

    @Override
    public void close() {
        
    }


    private static final boolean TRACE = false;

    @Override
    public int process(Buffer inputBuffer, Buffer outputBuffer) {
        byte [] inputContent=new byte[inputBuffer.getLength()];

        System.arraycopy(((byte[])inputBuffer.getData()),inputBuffer.getOffset(),inputContent,0,inputContent.length);


        byte[] mergedContent = mergeArrays((byte[]) innerBuffer.getData(), inputContent);
        innerBuffer.setData(mergedContent);
        innerBuffer.setLength(mergedContent.length);
        innerDataLength = innerBuffer.getLength();
        inputDataLength = inputBuffer.getLength();

        if (TRACE) dump("input ", inputBuffer);

        if (!checkInputBuffer(inputBuffer)) {
            return BUFFER_PROCESSED_FAILED;
        }

        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);    // TODO: what about data? can there be any?
            return BUFFER_PROCESSED_OK;
        }

        if (TRACE) dump("input ", inputBuffer);

        if (!checkInputBuffer(inputBuffer)) {
            return BUFFER_PROCESSED_FAILED;
        }

        if (isEOM(inputBuffer)) {
            propagateEOM(outputBuffer);    // TODO: what about data? can there be any?
            return BUFFER_PROCESSED_OK;
        }

        final int result;
        byte[] outputBufferData = (byte[]) outputBuffer.getData();

        if (outputBufferData == null || outputBufferData.length < PCM_BYTES * innerBuffer.getLength() / GSM_BYTES) {
            outputBufferData = new byte[PCM_BYTES * (innerBuffer.getLength() / GSM_BYTES)];
            outputBuffer.setData(outputBufferData);
        }

        if (innerBuffer.getLength() < GSM_BYTES) {
            result = OUTPUT_BUFFER_NOT_FILLED;
        } else {
            final boolean bigEndian = ((AudioFormat) outputFormat).getEndian() == AudioFormat.BIG_ENDIAN;

            outputBufferData = new byte[PCM_BYTES * (innerBuffer.getLength() / GSM_BYTES)];
            outputBuffer.setData(outputBufferData);
            outputBuffer.setLength(PCM_BYTES * (innerBuffer.getLength() / GSM_BYTES));

            GSMDecoderUtil.gsmDecode(bigEndian, (byte[]) innerBuffer.getData(), inputBuffer.getOffset(), innerBuffer.getLength(), outputBufferData);

            outputBuffer.setFormat(outputFormat);
            result = BUFFER_PROCESSED_OK;
            byte[] temp = new byte[innerDataLength - (innerDataLength / GSM_BYTES) * GSM_BYTES];
            innerContent = (byte[]) innerBuffer.getData();
            System.arraycopy(innerContent, (innerDataLength / GSM_BYTES) * GSM_BYTES, temp, 0, temp.length);
            outputBuffer.setOffset(0);

            innerBuffer.setLength(temp.length);
            innerBuffer.setData(temp);
        }

        if (TRACE) {
            dump("input ", inputBuffer);
            dump("output", outputBuffer);

            System.out.println("Result=" + LoggingStringUtils.plugInResultToStr(result));
        }
        return result;
    }

    private byte[] mergeArrays(byte[] arr1, byte[] arr2) {
        if (arr1 == null) return arr2;
        if (arr2 == null) return arr1;
        byte[] merged = new byte[arr1.length + arr2.length];
        System.arraycopy(arr1, 0, merged, 0, arr1.length);
        System.arraycopy(arr2, 0, merged, arr1.length, arr2.length);
        return merged;
    }

    	

    @Override
    public Format setInputFormat(Format arg0) {
        // TODO: force sample size, etc
        return super.setInputFormat(arg0);
    }


}