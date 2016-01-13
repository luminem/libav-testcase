package it.luminem;

import org.bytedeco.javacpp.*;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;

public class LibavFrameRecorder {

    private static void dummyVideo(String filename, String encoderName, String formatName) {
        int ret;
        Loader.load(org.bytedeco.javacpp.avutil.class);
        Loader.load(org.bytedeco.javacpp.avcodec.class);
        av_register_all();
        AVCodec codec = avcodec_find_encoder_by_name(encoderName);
        if (codec == null) {
            throw new RuntimeException("Bad encoder name: " + encoderName);
        }
        AVOutputFormat format = av_guess_format(formatName, null, null);
        if (format == null) {
            throw new RuntimeException("Bad format name: " + formatName);
        }
        AVFormatContext oc = avformat_alloc_context();
        oc.filename().putString(filename);
        oc.oformat(format);

        AVStream st = avformat_new_stream(oc, codec);
        AVCodecContext c = st.codec();
        c.codec_id(codec.id());
        c.codec_type(AVMEDIA_TYPE_VIDEO);
        c.pix_fmt(AV_PIX_FMT_YUV420P);

        AVRational frame_rate = new AVRational();
        frame_rate.num(30);
        frame_rate.den(1);
        st.time_base(av_inv_q(frame_rate));
        c.time_base(av_inv_q(frame_rate));
        c.width(640);
        c.height(480);
        ret = avcodec_open2(c, codec, (PointerPointer)null);
        if (ret < 0) {
            throw new RuntimeException("avcodec_open2() error " + ret);
        }

        av_dump_format(oc, 0, filename, 1);

        AVIOContext pb = new AVIOContext(null);
        if ((ret = avio_open(pb, filename, AVIO_FLAG_WRITE)) < 0) {
            //release();
            throw new RuntimeException("avio_open error() error " + ret + ": Could not open '" + filename + "'");
        }
        oc.pb(pb);

        ret = avformat_write_header(oc, (PointerPointer)null);
        if (ret < 0) {
            throw new RuntimeException("avformat_write_header() error " + ret);
        }

        AVFrame frame = av_frame_alloc();
        frame.format(c.pix_fmt());
        frame.width(c.width());
        frame.height(c.height());
        av_frame_get_buffer(frame, 32);

        frame.pts(0);

        AVPacket pkt = av_packet_alloc();
        for (int i=0;i<1000;i++) {
            int[] gotPacket = {0};
            avcodec_encode_video2(c, pkt, frame, gotPacket);
            if (gotPacket[0] != 0) {
                av_interleaved_write_frame(oc, pkt);
            }
            frame.pts(i);
        }


        av_write_trailer(oc);
        //toc.close();
    }

    public static void main(String[] args) throws Exception {
        dummyVideo("output.mpeg", "mpeg2video", "mpeg");
        dummyVideo("output.mkv", "libx264", "matroska");
    }

}
