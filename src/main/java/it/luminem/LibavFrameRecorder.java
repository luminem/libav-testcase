package it.luminem;

import org.bytedeco.javacpp.*;

import java.lang.reflect.Field;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;

public class LibavFrameRecorder {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        System.setProperty("java.library.path", System.getProperty("java.library.path")
                + ":/home/federico/repos/javacpp-presets/libav/cppbuild/linux-x86_64/lib"
                + ":/home/federico/repos/javacpp-presets/libav/target/classes/org/bytedeco/javacpp/linux-x86_64");
        final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
        sysPathsField.setAccessible(true);
        sysPathsField.set(null, null);
        System.out.println(System.getProperty("java.library.path"));
        Loader.load(org.bytedeco.javacpp.avutil.class);
        Loader.load(org.bytedeco.javacpp.avcodec.class);
        av_register_all();
        AVCodec codec = avcodec_find_decoder_by_name("mpeg2video");
        System.out.println("GOT CODEC: " + codec);
    }

}
