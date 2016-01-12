package it.luminem;

import org.bytedeco.javacpp.*;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;

public class LibavFrameRecorder {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        String filename = "output.mpeg";
        Loader.load(org.bytedeco.javacpp.avutil.class);
        Loader.load(org.bytedeco.javacpp.avcodec.class);
        av_register_all();
        AVCodec codec = avcodec_find_encoder_by_name("mpeg2video");
        AVOutputFormat format = av_guess_format("mpeg", null, null);
        AVFormatContext oc = avformat_alloc_context();
        oc.filename().putString(filename);
        System.out.println("OC " + oc);
        AVStream st = avformat_new_stream(oc, codec);
        st.codec().codec_id(codec.id());
        st.codec().codec_type(AVMEDIA_TYPE_VIDEO);
        st.codec().pix_fmt(AV_PIX_FMT_YUV420P);
        AVRational frame_rate = new AVRational();
        frame_rate.num(30);
        frame_rate.den(1);
        st.time_base(av_inv_q(frame_rate));
        st.codec().time_base(av_inv_q(frame_rate));
        avcodec_open2(st.codec(), null, (PointerPointer)null);

        int ret;
        AVIOContext pb = new AVIOContext(null);
        if ((ret = avio_open(pb, "output.mpeg", AVIO_FLAG_WRITE)) < 0) {
            //release();
            throw new RuntimeException("avio_open error() error " + ret + ": Could not open '" + filename + "'");
        }
        oc.pb(pb);

        avformat_write_header(oc, new AVDictionary());
    }

}
