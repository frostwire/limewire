package net.sf.fmj.media.codec.audio.gsm;

import net.sf.fmj.media.AbstractCodec;
import net.sf.fmj.utility.LoggerSingleton;
import net.sf.fmj.utility.LoggingStringUtils;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;
import java.util.logging.Logger;


/**
 * GSM encoder Codec. Encodes 160 16-bit PCM samples into array of 33 bytes (GSM frame).
 *
 * @author Martin Harvan
 */
public class Encoder extends AbstractCodec {
    private static final Logger logger = LoggerSingleton.logger;
    private Buffer innerBuffer = new Buffer();
    private static final int PCM_BYTES = 320;
    private static final int GSM_BYTES = 33;
    private int innerDataLength = 0;
    private int inputDataLength = 0;
    byte[] innerContent;

    @Override
    public String getName() {
        return "GSM Encoder";
    }

    public Encoder() {
        super();
        this.inputFormats = new Format[]{
                new AudioFormat(AudioFormat.LINEAR, 8000, 16, 1, AudioFormat.BIG_ENDIAN, AudioFormat.SIGNED, -1, -1.0, Format.byteArray)
        };

    }

    // TODO: move to base class?
    protected Format[] outputFormats = new Format[]{new AudioFormat(
            AudioFormat.GSM, 8000, 8, 1, -1, AudioFormat.SIGNED, 264, -1, Format.byteArray)};

    @Override
    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) {
            return outputFormats;
        } else {
            if (!(input instanceof AudioFormat)) {
                logger.warning(this.getClass().getSimpleName() + ".getSupportedOutputFormats: input format does not match, returning format array of {null} for " + input); // this can cause an NPE in JMF if it ever happens.
                return new Format[]{null};
            }
            final AudioFormat inputCast = (AudioFormat) input;
            final AudioFormat result = new AudioFormat(AudioFormat.GSM, inputCast.getSampleRate(), 8,
                    1, inputCast.getEndian(), AudioFormat.SIGNED, 264,
                    inputCast.getFrameRate(), Format.byteArray);

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
        

        byte[] mergedContent = mergeArrays(
                (byte[]) innerBuffer.getData(), inputContent);
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

        final int result;
        byte[] outputBufferData = (byte[]) outputBuffer.getData();

        if (outputBufferData == null || outputBufferData.length < GSM_BYTES * innerDataLength / PCM_BYTES) {
            outputBufferData = new byte[GSM_BYTES * (innerDataLength / PCM_BYTES)];
            outputBuffer.setData(outputBufferData);
        }

        if (innerDataLength < PCM_BYTES) {
            result = OUTPUT_BUFFER_NOT_FILLED;
            System.out.println("Not filled");
        } else {
            final boolean bigEndian = ((AudioFormat) outputFormat).getEndian() == AudioFormat.BIG_ENDIAN;

            outputBufferData = new byte[GSM_BYTES * (innerDataLength / PCM_BYTES)];
            outputBuffer.setData(outputBufferData);
            outputBuffer.setLength(GSM_BYTES * (innerDataLength / PCM_BYTES));

            GSMEncoderUtil.gsmEncode(bigEndian, (byte[]) innerBuffer.getData(), innerBuffer.getOffset(), innerDataLength, outputBufferData);

            outputBuffer.setFormat(outputFormat);
            outputBuffer.setData(outputBufferData);
            result = BUFFER_PROCESSED_OK;
            byte[] temp = new byte[innerDataLength - (innerDataLength / PCM_BYTES) * PCM_BYTES];
            innerContent = (byte[]) innerBuffer.getData();
            System.arraycopy(innerContent, (innerDataLength / PCM_BYTES) * PCM_BYTES, temp, 0, temp.length);

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
        //TODO: force sample size, etc
        return super.setInputFormat(arg0);
    }

    @Override
    public Format setOutputFormat(Format arg0) {
        return super.setOutputFormat(arg0);
    }


}
