// SystemImages.java
// Copyright 1996, 1997 Netscape Communications Corp.  All rights reserved.

package netscape.application;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.Image;
/**
  * @note 1.0 changes for Keyboard UI
  */
class SystemImages {
    private SystemImages() {
    }

    /*
    static Bitmap bitmapForEncoding(int width, int height, long rle[]) {
        int i, j, len, pix, pixels[];

        pixels = new int[width * height];

        for (i = 0, j = 0; i < rle.length; i++) {
            len = (int)(rle[i] >>> 32L);
            pix = (int)(rle[i] & 0xffffffffL);

            while (len-- > 0)
                pixels[j++] = pix;
        }

        return bitmapForPixels(width, height, pixels);
    }
    */

    static int pixelForMapColor(int index, long map[]) {
        if ((index % 2) == 0)
            return (int)(map[index / 2] >>> 32L);
        else
            return (int)(map[index / 2] & 0xffffffffL);
    }

    static Bitmap bitmapForEncoding(int w, int h, long rle[], long map[]) {
        int i, len, pix, pixCount, pixels[];
        long chunk, shift;

        pixels = new int[w * h];
        pixCount = 0;

        for (i = 0; i < rle.length; i++) {
            chunk = rle[i];
            shift = 56L;

            while (shift > 0L) {
                len = (int)((chunk >>> shift) & 0xffL);
                if (len == 0)
                    break;

                shift -= 8L;
                pix = pixelForMapColor((int)((chunk >>> shift) & 0xffL), map);

                while (len-- > 0)
                    pixels[pixCount++] = pix;

                shift -= 8L;
            }
        }

        if (pixCount != (w * h))
            throw new Error("Bad Image!");

        return bitmapForPixels(w, h, pixels);
    }

    static Bitmap bitmapForPixels(int width, int height, int pixels[]) {
        java.awt.Toolkit toolkit;
        Image image;
        MemoryImageSource src;

        toolkit = AWTCompatibility.awtToolkit();
        src = new MemoryImageSource(width, height, ColorModel.getRGBdefault(),
                                    pixels, 0, width);
        image = toolkit.createImage(src);

        return new Bitmap(image);
    }

    static Bitmap redGrad() {
        int i, pixels[] = new int[256];

        for (i = 0; i < 256; i++)
            pixels[i] = 0xff000000 | i << 16;

        return bitmapForPixels(256, 1, pixels);
    }

    static Bitmap greenGrad() {
        int i, pixels[] = new int[256];

        for (i = 0; i < 256; i++)
            pixels[i] = 0xff000000 | i << 8;

        return bitmapForPixels(256, 1, pixels);
    }

    static Bitmap blueGrad() {
        int i, pixels[] = new int[256];

        for (i = 0; i < 256; i++)
            pixels[i] = 0xff000000 | i;

        return bitmapForPixels(256, 1, pixels);
    }

    static Bitmap alertWarning() {
        Bitmap bitmap;
        int width = 39;
        int height = 34;
        long map[] = {
            0xffc6c6c6ffcf9788L, 0xffc8bdbaffdf4219L, 0xffd8684affd57b63L,
            0xffe33001ffcbaaa1L, 0xffe1390dffdc5532L, 0xffd18e7cffcda095L,
            0xffde4c25ffcab3adL, 0xffd3846fffb59b95L, 0xffbeb1adffda5e3eL,
            0xffc2bbbaff913820L, 0xff972e14ff892308L, 0xffad867cff982d11L,
            0xffff633affe0512cL, 0xff882409ffa73618L, 0xffef5a33ff882309L,
            0xff90280eff984029L, 0xff831c01ffc13f1dL, 0xffa06657ffd67157L,
            0xff92361dff9a4d38L, 0xffd04825ffa47163L, 0xffb93b1aff8d2d13L,
            0xffb19188ff9f5744L, 0xffb9a6a1ffd84d28L, 0xffb03819ff95432dL,
            0xffa97b6ffff75f36L
        };
        long rle[] = {
            0x1300010101022500L, 0x0103010424000105L, 0x0206010722000102L,
            0x0108020601092200L, 0x01040406010a2000L, 0x010b050601030102L,
            0x1f0001090206010cL, 0x030601051e00010aL, 0x0206010801020109L,
            0x02060108010d1c00L, 0x010201030206010eL, 0x0100010703060104L,
            0x1c0001050206010cL, 0x030001040306010bL, 0x1a00010d01080206L,
            0x010b0100010f0110L, 0x010201080206010cL, 0x1a00011102060111L,
            0x0112011301140115L, 0x011601050306010aL, 0x1800010b0306010dL,
            0x0116011701180119L, 0x011a010001030206L, 0x010801021700010cL,
            0x0206010501000116L, 0x011b0118011c011dL, 0x0100010103060105L,
            0x1600010a02060108L, 0x01020100010f011eL, 0x01180119011f0200L,
            0x01090306010d1400L, 0x010201080206010aL, 0x0300012001180121L,
            0x01220200010d0306L, 0x0109140001230206L, 0x0109040001240119L,
            0x011b011603000123L, 0x0306010b1200010dL, 0x0306010b04000125L,
            0x01260114010f0300L, 0x0102010802060103L, 0x1200010902060104L,
            0x0500012701280120L, 0x0500010e0306010eL, 0x1000010b02060108L,
            0x010d0500010f0120L, 0x01290600010c0206L, 0x010801020f000103L,
            0x0206010506000110L, 0x012001250600010bL, 0x030601230e00010eL,
            0x020601030800011aL, 0x0127070001110306L, 0x01070c0001020108L,
            0x020601010800012aL, 0x01120700010d0306L, 0x01090c0001040206L,
            0x0109130001050306L, 0x01010a0001070306L, 0x01070800012a011fL,
            0x012b011207000102L, 0x0108020601030a00L, 0x0109020601040800L,
            0x012c0115012d012eL, 0x012f0800010a0306L, 0x0105080001010206L,
            0x0108010208000130L, 0x011b011801310120L, 0x0900010902060108L,
            0x0102070001030206L, 0x010e09000110011dL, 0x0128011b012b0900L,
            0x010b030601040600L, 0x0105020601030b00L, 0x012c012201300112L,
            0x0a0001040306010bL, 0x0400010d01080206L, 0x010b1900010d0108L,
            0x0206010904000104L, 0x2206010a0200010bL, 0x2306010301020100L,
            0x010924060105010dL, 0x2505010e00000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap alertQuestion() {
        Bitmap bitmap;
        int width = 35;
        int height = 33;
        long map[] = {
            0xffc6c6c6ffb7bfadL, 0xff91ad6fff7aa24aL, 0xff6b9b32ff4d8d01L,
            0xff639825ff89a963L, 0xffafbba1ff98b17cL, 0xff5c9419ffbec2baL,
            0xffa8b895ff54900dL, 0xff82a657ff729f3eL, 0xffadb5a4ffa5af98L,
            0xffb5baafff688643L, 0xff416a10ff59802bL, 0xff4e751fff4f7522L,
            0xff83986bff8c9e76L, 0xff547a25ff7da44dL, 0xff98bf69ff88af58L,
            0xff5e852fff587b2fL, 0xffbec0bbff476f18L, 0xff8db45eff729a42L,
            0xff6c943cff628934L, 0xff7a925dffa0b488L, 0xff82aa53ff5f8137L,
            0xff587f2bff93ba63L, 0xff486f18ff699039L, 0xff4d751dff698745L,
            0xff779f47ff608038L, 0xff587b2dff60803aL, 0xff829868ff92a37eL,
            0xff527a24ff507523L, 0xff4e741fff476f17L, 0xff94a381ff698646L,
            0xff718c51ff587f29L, 0xff678f3700000000L
        };
        long rle[] = {
            0x0b00010101020103L, 0x0104040501060104L, 0x0107010815000109L,
            0x01040c05010a0107L, 0x010b1000010c0106L, 0x1005010d01020e00L,
            0x010e14050104010bL, 0x0a00010b01040605L, 0x01060107010c010bL,
            0x0300010c0109010fL, 0x0605010d01080800L, 0x010b010605050103L,
            0x0101030001100311L, 0x01120200010b0107L, 0x010d0405010d0108L,
            0x0700010f0405010aL, 0x010c020001110113L, 0x0114041501160117L,
            0x01180200010b0104L, 0x0405010a010b0500L, 0x010204050106010bL,
            0x02000119011a011bL, 0x061c011d011e011fL, 0x0120020001030405L,
            0x010f04000101010dL, 0x03050106010b0300L, 0x0121022201230224L,
            0x011b041c01250126L, 0x0300010304050127L, 0x030001030305010dL,
            0x010103000111011aL, 0x0128012101260218L, 0x0129012a031c012bL,
            0x012c040001040305L, 0x010a0200010b010dL, 0x0305010204000119L,
            0x012d012e01110400L, 0x012f0130031c0115L, 0x0111030001010405L,
            0x0127010001090305L, 0x010a050001190114L, 0x0131050001180124L,
            0x031c012c01200400L, 0x01030305010f0100L, 0x010f0305010e0d00L,
            0x01320128021c0123L, 0x0133050001080305L, 0x010d0100010a0305L,
            0x010c0c000134012aL, 0x021c0130012c0112L, 0x0600010d03050108L,
            0x04050c0001350136L, 0x022b012d01370110L, 0x070001040305010cL,
            0x04050b0001350136L, 0x012b012801380126L, 0x0120080001040305L,
            0x012704050a000111L, 0x0139012b012d0137L, 0x01110a00010f0305L,
            0x010904050a00012fL, 0x0130012d011f0120L, 0x0b0001040305010cL,
            0x010d030501080900L, 0x0129013001140120L, 0x0c00010a0305010cL,
            0x0104030501020900L, 0x0134012101350c00L, 0x0101040501000102L,
            0x0305010618000107L, 0x0305010401000101L, 0x0405010c0900013aL,
            0x013b011901200900L, 0x010b010d03050109L, 0x0200010f0305010aL,
            0x010b0700013c012aL, 0x01230125011f0120L, 0x0800010e0305010dL,
            0x010b0200010c0405L, 0x0103060001120139L, 0x012b021c013d013aL,
            0x0700010904050107L, 0x0400010e04050103L, 0x05000111011a031cL,
            0x013e011806000109L, 0x040501060500010bL, 0x01060405010f010bL,
            0x040001370130011cL, 0x011d012c01120500L, 0x01070405010d0108L,
            0x06000108010d0405L, 0x010d010903000110L, 0x01370114012c013aL,
            0x0400010c01040505L, 0x010908000108010dL, 0x0505010a010e010cL,
            0x0600010101020106L, 0x060501090a00010bL, 0x0106080501060304L,
            0x0805010d010c0d00L, 0x0107010d1105010fL, 0x010b0f0001010103L,
            0x010d0d050104010cL, 0x1300010b0109010fL, 0x010a070501040102L,
            0x01011a00040c010bL, 0x0f00000000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }
    static Bitmap alertNotification() {
        Bitmap bitmap;
        int width = 37;
        int height = 34;
        long map[] = {
            0xffc6c6c6ffa4a5c8L, 0xff8183caff7073caL, 0xff5f62cbff4549cdL,
            0xff3d41cdff676acbL, 0xff9b9cc8ff4e51ccL, 0xffbdbec6ff797bcaL,
            0xffb5b5c7ff565accL, 0xff8a8cc9ffacadc7L, 0xffb4b4bfff7d7eacL,
            0xffa1a2b9ff8f90b2L, 0xff3a3d96ff5255c0L, 0xff5457c8ff40429dL,
            0xff686aa7ff686ceaL, 0xff7478ffff7074f8L, 0xff3e419dff9899b6L,
            0xff494ca9ff9294c9L, 0xffababbcff3f429dL, 0xff4e51b9ff8687afL,
            0xff5457a1ff494cb2L, 0xff6c70f1ff3b3e97L, 0xffbdbdc3ff3b3d97L,
            0xff343792ff5e61a4L, 0xff4a4c9fff7a7cadL, 0xff3c3f9fff6064ddL,
            0xff5355a3ff6a6ca5L, 0xff484baaff585bcfL, 0xff7173aaff7475a9L,
            0xff5053c1ff6468e4L, 0xff393c97ff5053b8L, 0xff5c5fa6ff5c60d6L,
            0xff42449bff4346a4L, 0xff5658a0ff464aabL, 0xff4d4fb000000000L
        };
        long rle[] = {
            0x0d00010101020103L, 0x0404010301020101L, 0x1800010101030105L,
            0x0b06010701081300L, 0x0101010410060109L, 0x01010f00010a010bL,
            0x14060107010a0c00L, 0x010c010d06060109L, 0x010301020401010eL,
            0x0103010906060105L, 0x010f0a00010f0105L, 0x0506010401010300L,
            0x0110021101120300L, 0x010f010305060105L, 0x010f0800010a0105L,
            0x0406010901010400L, 0x0113011401150116L, 0x011701180400010fL,
            0x010d04060105010fL, 0x0700010d04060103L, 0x010a040001100114L,
            0x0119021a011b011cL, 0x011d0500010b0406L, 0x0109010a05000102L,
            0x0406010306000112L, 0x011e041a01160111L, 0x0600011f04060103L,
            0x0400010c04060107L, 0x070001200121041aL, 0x0122012307000103L,
            0x0406010103000103L, 0x03060105010a0800L, 0x012401250126011bL,
            0x0116012701280800L, 0x010d030601040200L, 0x010a01050306011fL,
            0x0900012801180129L, 0x012a012b01200900L, 0x01010406010c0100L,
            0x0108030601091a00L, 0x010d0306010e0100L, 0x01030306010b1a00L,
            0x010e030601040100L, 0x0104030601010c00L, 0x0128021201100a00L,
            0x010c030601050100L, 0x0406010a09000128L, 0x0123012c012a021eL,
            0x011401130a000406L, 0x010004060900012dL, 0x0114012a012e0119L,
            0x021a012f01300a00L, 0x01090306010f0406L, 0x0900011302110131L,
            0x0132021a01330134L, 0x0a00010d03060101L, 0x04060c0001350136L,
            0x021a0132011d0a00L, 0x04060100010d0306L, 0x01010b00012c0137L,
            0x011a011b012a0128L, 0x0900010a04060100L, 0x01070306010e0a00L,
            0x01100138021a012fL, 0x01240a00011f0306L, 0x01040100011f0306L,
            0x010d0a0001230139L, 0x021a012201230a00L, 0x01070306010b0100L,
            0x010c040601010900L, 0x013a013b021a0117L, 0x01200900010c0406L,
            0x010f020001070306L, 0x010d0900012a011bL, 0x011a0119013c0a00L,
            0x0107030601090300L, 0x0101040601020700L, 0x011d013d021a0116L,
            0x01340900011f0406L, 0x011f040001070406L, 0x011f060001350116L,
            0x021a013201110112L, 0x0111012b01130400L, 0x010f040601040500L,
            0x010a01050406011fL, 0x050001300137021aL, 0x0133013d022a013eL,
            0x0128030001080105L, 0x03060105010f0600L, 0x010f05060107010aL,
            0x03000134013f0216L, 0x01400114012b0112L, 0x0300010a010b0506L,
            0x011f0800011f0506L, 0x01050102010a0200L, 0x0113021101130110L,
            0x0400011f01090506L, 0x011f0a0001080105L, 0x050601050103011fL,
            0x01010400010f011fL, 0x010301050606011fL, 0x0c00010f010d1406L,
            0x010901010f000102L, 0x010511060102010aL, 0x1100010a01020109L,
            0x0c060105010b010cL, 0x1600010f01020104L, 0x0109040601090104L,
            0x010201010d000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }


    static Bitmap alertStripe() {
        Bitmap bitmap;
        int width = 33;
        int height = 20;
        long map[] = {
            0xffffffffff000000L, 0xff2e2500ffe9bf01L, 0xffffd101fffdcf01L,
            0xff695600ff2d2500L, 0xfffed001ff6a5600L, 0xffeac001ff2c2400L,
            0xff695700ff6a5700L, 0xfffdd001ff2d2400L, 0xff99999900000000L
        };
        long rle[] = {
            0x21000e0101020103L, 0x0e04010501061001L, 0x010701030e040108L,
            0x010901060f010102L, 0x010a0e0401050104L, 0x01060f01010b0103L,
            0x0f040105010c0f01L, 0x0107010a0f040108L, 0x01060f0101070103L,
            0x100401060f01010bL, 0x01030f040105010cL, 0x0f010107010a0f04L,
            0x010801060f010107L, 0x01030f0401080109L, 0x0f01010701030f04L,
            0x010501060f010102L, 0x010a100401060f01L, 0x010b01030f040105L,
            0x010c0f010107010aL, 0x0f04010801060f01L, 0x010701030f040105L,
            0x010d0f010107010aL, 0x0f040108010c0f01L, 0x010701030f04010eL,
            0x010d0f01010f010aL, 0x0f04010e01090f01L, 0x0102010321100000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }



    static Bitmap checkMark() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00c0c0c0ffbcbcbcL, 0xffc4c4c4ff3c3c3cL, 0xffccccccff0c0c0cL,
            0xfffffffffff4f4f4L, 0xffd4d4d4ffacacacL, 0xffb4b4b4ff9c9c9cL,
            0xff141414ff545454L, 0xff2c2c2cff949494L, 0xffe4e4e4ffdcdcdcL,
            0xff747474ff444444L, 0xff848484ff7c7c7cL
        };
        long rle[] = {
            0x2b00010101020d00L, 0x0101010201030104L, 0x0b00020101050106L,
            0x0b00020101050107L, 0x01040a0001010104L, 0x0105010801070109L,
            0x0600010103000108L, 0x0105010a01060109L, 0x06000101010b0101L,
            0x01000108010c0109L, 0x0106010106000101L, 0x0109010d01090108L,
            0x010e010f01100111L, 0x0109070001020112L, 0x0108011301140104L,
            0x0106010908000101L, 0x0114010901020101L, 0x010601090a000101L,
            0x0115010101060e00L, 0x01070f0001091a00L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap closeButton() {
        Bitmap bitmap;
        int width = 20;
        int height = 20;
        long map[] = {
            0x00c0c0c0ff999999L, 0xff808080ffccccccL, 0xffffffffffc0c0c0L,
            0xff66666600000000L
        };
        long rle[] = {
            0x6b00010101020201L, 0x0f00010201030104L, 0x020301010d000101L,
            0x0103020402030105L, 0x01030c0001010204L, 0x0103010501030101L,
            0x01030c0001010104L, 0x0203010502010103L, 0x0c00030301050103L,
            0x0101010201030e00L, 0x0103010101020106L, 0x0103100003039500L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap closeButtonActive() {
        Bitmap bitmap;
        int width = 20;
        int height = 20;
        long map[] = {
            0x00c0c0c0ffccccccL, 0xff666666ff808080L, 0xff999999ffc0c0c0L,
            0xffffffff00000000L
        };
        long rle[] = {
            0x6b00030110000101L, 0x0102010301040101L, 0x0e00010101030104L,
            0x0101010503010c00L, 0x0101020401050201L, 0x010601040c000101L,
            0x0104010101050101L, 0x020601040c000101L, 0x0105020102060101L,
            0x01040d0001040201L, 0x0106010101030f00L, 0x0204010301049500L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap colorScrollKnob() {
        Bitmap bitmap;
        int width = 17;
        int height = 7;
        long map[] = {
            0xffc5c5c5ffb4b4b4L, 0x00ffffffffccccccL, 0xff9b9b9bff5f5f5fL,
            0xff676767ff919191L, 0xff828282ff7f7f7fL
        };
        long rle[] = {
            0x0100010101000a02L, 0x0103010001010300L, 0x01020b0301040100L,
            0x0101010501020103L, 0x0100010102000101L, 0x0200010102000101L,
            0x0204010601010203L, 0x0300010102000101L, 0x0200010101000104L,
            0x0107010101020106L, 0x0108010101000101L, 0x0200010102000101L,
            0x0200010901060102L, 0x0100010101060a08L, 0x0109010601010100L,
            0x010102000b060300L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap popupKnob() {
        Bitmap bitmap;
        int width = 9;
        int height = 9;
        long map[] = {
            0x00c0c0c0ffd6d6d6L, 0xffadadadff969696L, 0xfff4f4f4ffdadadaL,
            0xffb4b4b4ff8f8f8fL, 0xffebebebffb7b7b7L, 0xffa2a2a2ff808080L,
            0xffe0e0e0ffcbcbcbL, 0xff7f7f7fff6f6f6fL, 0xffa5a5a5ff656565L,
            0xff404040ff979797L
        };
        long rle[] = {
            0x0300010101020103L, 0x0500010401050106L, 0x0207040001080109L,
            0x010a0107010b0107L, 0x0200010c010d0103L, 0x0207010e010b0200L,
            0x010d01020307020fL, 0x0200010601100307L, 0x0211020001090110L,
            0x0207010f01120111L, 0x03000113010b010fL, 0x02120500010e0111L,
            0x0112030000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap popupKnobH() {
        Bitmap bitmap;
        int width = 9;
        int height = 9;
        long map[] = {
            0x00c0c0c0ffd6d6d6L, 0xffadadadff969696L, 0xfff4f4f4ffdadadaL,
            0xffb4b4b4ff8f8f8fL, 0xffebebebffb7b7b7L, 0xffa2a2a2ff808080L,
            0xffe0e0e0ffcbcbcbL, 0xff7f7f7fff6f6f6fL, 0xffa5a5a5ff656565L,
            0xff404040ff979797L
        };
        long rle[] = {
            0x0300010101020103L, 0x0500010401050106L, 0x0207040001080109L,
            0x010a0107010b0107L, 0x0200010c010d0103L, 0x0207010e010b0200L,
            0x010d01020307020fL, 0x0200010601100307L, 0x0211020001090110L,
            0x0207010f01120111L, 0x03000113010b010fL, 0x02120500010e0111L,
            0x0112030000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap radioButtonOff() {
        Bitmap bitmap;
        int width = 15;
        int height = 15;
        long map[] = {
            0x00c0c0c0ffb2b2b2L, 0xffb7b7b7ffbcbcbcL, 0xffc0c0c0ffe0e0e0L,
            0xfff0f0f0ffe1e1e1L, 0xffd2d2d2ffadadadL, 0xffc3c3c3fff4f4f4L,
            0xffe9e9e9ffdadadaL, 0xffcbcbcbffb4b4b4L, 0xff8f8f8fffd6d6d6L,
            0xffa5a5a5ff696969L, 0xffccccccff656565L, 0xff9e9e9effebebebL,
            0xff808080ff404040L, 0xff969696ff7f7f7fL, 0xff979797ffa2a2a2L
        };
        long rle[] = {
            0x0500010102020103L, 0x0104080001010105L, 0x0106010701080104L,
            0x01090104010a0500L, 0x0104020b010c010dL, 0x0108010e010f0110L,
            0x0102010a03000102L, 0x030b010c0111010eL, 0x010a010201120113L,
            0x0103010a0200040bL, 0x0314010801040109L, 0x011001150200010fL,
            0x030b010e040a0114L, 0x0112011001150116L, 0x0100010a01060117L,
            0x010e060a01090110L, 0x02150100010a0107L, 0x010d010e060a0103L,
            0x0118011501190100L, 0x010201110108010eL, 0x050a010e011a011bL,
            0x0115011901000103L, 0x0104010e010a0104L, 0x0114030a010f0118L,
            0x0215011902000102L, 0x01090102010f0109L, 0x010a0114010f0110L,
            0x0215011901180300L, 0x011201100216011cL, 0x0110011b03150119L,
            0x0500010f01130515L, 0x0119011506000104L, 0x0100011d011b0215L,
            0x0113011d13000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap radioButtonOn() {
        Bitmap bitmap;
        int width = 15;
        int height = 15;
        long map[] = {
            0x00c0c0c0ffb2b2b2L, 0xffb7b7b7ffbcbcbcL, 0xffe0e0e0fff0f0f0L,
            0xffe1e1e1ffd2d2d2L, 0xffadadadffc3c3c3L, 0xfff4f4f4ffe9e9e9L,
            0xffdadadaffcbcbcbL, 0xffb4b4b4ff8f8f8fL, 0xff979797ffa5a5a5L,
            0xff696969ff808080L, 0xff818180ff656565L, 0xff929191ffa7a7a7L,
            0xff9e9e9effa1a2a2L, 0xffb7b8b8ffcececeL, 0xff9c9c9cffc9c8c8L,
            0xffdededeff404040L, 0xffd6d6d6ffadadacL, 0xffc3c2c2ffd9d9d9L,
            0xff7f7f7fffa7a8a7L, 0xffbdbdbdffd3d3d3L, 0xffa2a2a1ffb8b7b8L,
            0xffcdceceffe3e4e4L, 0xffa2a2a200000000L
        };
        long rle[] = {
            0x0500010102020103L, 0x0900010101040105L, 0x0106010701000108L,
            0x010001090600020aL, 0x010b010c0107010dL, 0x010e010f01020109L,
            0x03000102020a0100L, 0x0310010901020111L, 0x0112010301090200L,
            0x020a010004130114L, 0x01000108010f0115L, 0x0200010e010a0100L,
            0x0413011001160117L, 0x0111010f01150118L, 0x0100010901050100L,
            0x031302100119011aL, 0x011b010f02150100L, 0x0109010601000213L,
            0x0210011c0101011dL, 0x011e01130115011fL, 0x0100010201200100L,
            0x0213021001210122L, 0x0123010b01240115L, 0x011f010001030100L,
            0x010d010002100125L, 0x01260127010b0113L, 0x0215011f02000102L,
            0x01080102010e0128L, 0x0129012a012b010fL, 0x0215011f01130300L,
            0x0111010f02180110L, 0x010f01240315011fL, 0x0500010e01120515L,
            0x011f01150800012cL, 0x012402150112012cL, 0x1300000000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap resizeLeft() {
            Bitmap bitmap;
        int width = 11;
        int height = 11;
        long map[] = {
            0xff999999ffc6c6c6L, 0xff808080ffffffffL, 0xff66666600000000L
        };
        long rle[] = {
            0x0600050101000401L, 0x0300030101000601L, 0x0200020101000701L,
            0x0200010101000801L, 0x0100010101000201L, 0x0102010004010300L,
            0x0201010001010103L, 0x0401020003010203L, 0x0401020009010200L,
            0x090102000a040000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap resizeRight() {
        Bitmap bitmap;
        int width = 11;
        int height = 11;
        long map[] = {
            0xffc6c6c6ff999999L, 0xff666666ff808080L, 0xffffffff00000000L
        };
        long rle[] = {
            0x0500050101020300L, 0x0301040001020200L, 0x0201060001020100L,
            0x0201070001020100L, 0x0101080001020201L, 0x0300010301010300L,
            0x0102010104000101L, 0x0100010402000102L, 0x0101050002040200L,
            0x0102010109000102L, 0x010109000c020000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollDownArrow() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00c6c6c6ff808080L, 0xff66666600000000L
        };
        long rle[] = {
            0x63000a0107000101L, 0x0602010109000101L, 0x040201010b000101L,
            0x020201010d000201L, 0x5700000000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollDownArrowActive() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00c6c6c6ff636363L, 0xff54545400000000L
        };
        long rle[] = {
            0x52000a0107000101L, 0x0602010109000101L, 0x040201010b000101L,
            0x020201010d000201L, 0x6800000000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollKnobH() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00000000ff9c9c9cL, 0xffc0c0c0ffffffffL
        };
        long rle[] = {
            0x560003010c000201L, 0x03020b0001010102L, 0x0103010101020103L,
            0x0a00010101020201L, 0x010201030b000302L, 0x02030c0003035600L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollKnobV() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00000000ff9c9c9cL, 0xffc0c0c0ffffffffL
        };
        long rle[] = {
            0x560003010c000201L, 0x03020b0001010102L, 0x0103010101020103L,
            0x0a00010101020201L, 0x010201030b000302L, 0x02030c0003035600L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollLeftArrow() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00c6c6c6ff808080L, 0xff66666600000000L
        };
        long rle[] = {
            0x390001010e000201L, 0x0d00010101020101L, 0x0c00010102020101L,
            0x0b00010103020101L, 0x0b00010103020101L, 0x0c00010102020101L,
            0x0d00010101020101L, 0x0e0002010f000101L, 0x3600000000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollLeftArrowActive() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00c6c6c6ff636363L, 0xff54545400000000L
        };
        long rle[] = {
            0x280001010e000201L, 0x0d00010101020101L, 0x0c00010102020101L,
            0x0b00010103020101L, 0x0b00010103020101L, 0x0c00010102020101L,
            0x0d00010101020101L, 0x0e0002010f000101L, 0x4700000000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollRightArrow() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00c6c6c6ff808080L, 0xff66666600000000L
        };
        long rle[] = {
            0x360001010f000201L, 0x0e00010101020101L, 0x0d00010102020101L,
            0x0c00010103020101L, 0x0b00010103020101L, 0x0b00010102020101L,
            0x0c00010101020101L, 0x0d0002010e000101L, 0x3900000000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollRightArrowActive() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00c6c6c6ff636363L, 0xff54545400000000L
        };
        long rle[] = {
            0x250001010f000201L, 0x0e00010101020101L, 0x0d00010102020101L,
            0x0c00010103020101L, 0x0b00010103020101L, 0x0b00010102020101L,
            0x0c00010101020101L, 0x0d0002010e000101L, 0x4a00000000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollTrayBottom() {
        Bitmap bitmap;
        int width = 16;
        int height = 1;
        long map[] = {
            0xffc6c6c6ffe7e7e7L
        };
        long rle[] = {
            0x0100010101000101L, 0x0100010101000101L, 0x0100010101000101L,
            0x0100010101000101L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollTrayLeft() {
        Bitmap bitmap;
        int width = 1;
        int height = 16;
        long map[] = {
            0xffc6c6c6ff999999L
        };
        long rle[] = {
            0x0100010101000101L, 0x0100010101000101L, 0x0100010101000101L,
            0x0100010101000101L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollTrayRight() {
        Bitmap bitmap;
        int width = 1;
        int height = 16;
        long map[] = {
            0xffc6c6c6ffe7e7e7L
        };
        long rle[] = {
            0x0100010101000101L, 0x0100010101000101L, 0x0100010101000101L,
            0x0100010101000101L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollTrayTop() {
        Bitmap bitmap;
        int width = 16;
        int height = 1;
        long map[] = {
            0xffc6c6c6ff999999L
        };
        long rle[] = {
            0x0100010101000101L, 0x0100010101000101L, 0x0100010101000101L,
            0x0100010101000101L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollUpArrow() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00c6c6c6ff808080L, 0xff66666600000000L
        };
        long rle[] = {
            0x570002010d000101L, 0x020201010b000101L, 0x0402010109000101L,
            0x0602010107000a01L, 0x6300000000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap scrollUpArrowActive() {
        Bitmap bitmap;
        int width = 16;
        int height = 16;
        long map[] = {
            0x00c6c6c6ff636363L, 0xff54545400000000L
        };
        long rle[] = {
            0x460002010d000101L, 0x020201010b000101L, 0x0402010109000101L,
            0x0602010107000a01L, 0x7400000000000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap titleBarLeft() {
        Bitmap bitmap;
        int width = 5;
        int height = 20;
        long map[] = {
            0x00c0c0c0ff9c9c9cL
        };
        long rle[] = {
            0x1300010102000301L, 0x0100020103000101L, 0x0300020103000101L,
            0x0400010104000101L, 0x0400010104000201L, 0x0400010104000201L,
            0x0400010116000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap titleBarRight() {
        Bitmap bitmap;
        int width = 5;
        int height = 20;
        long map[] = {
            0x00c0c0c0ff9c9c9cL, 0xffffffff00000000L
        };
        long rle[] = {
            0x0f00010104000301L, 0x0400020104000101L, 0x0400020104000101L,
            0x0400010104000101L, 0x0400010108000102L, 0x0300020201000302L,
            0x0200010213000000L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap topLeftArrow() {
        Bitmap bitmap;
        int width = 9;
        int height = 13;
        long map[] = {
            0x00ffffffff2b8000L, 0xff2b5500ff80ff40L
        };
        long rle[] = {
            0x0700010101020600L, 0x0102010301010500L, 0x0102020302020101L,
            0x0102010101020303L, 0x0201070301020100L, 0x0102060301010200L,
            0x0102050301020300L, 0x0102040301010400L, 0x0102030301020500L,
            0x0102020301010600L, 0x0102010301020700L, 0x0102010108000102L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap topRightArrow() {
        Bitmap bitmap;
        int width = 9;
        int height = 13;
        long map[] = {
            0xff2b5500ff2b8000L, 0x00ffffffff80ff40L
        };
        long rle[] = {
            0x0100010107020101L, 0x0103010006020100L, 0x0203010005020101L,
            0x0303010001010100L, 0x0101020007030201L, 0x0603010001020100L,
            0x0503010002020101L, 0x0403010003020100L, 0x0303010004020101L,
            0x0203010005020100L, 0x0103010006020101L, 0x0100070201000802L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap bottomLeftArrow() {
        Bitmap bitmap;
        int width = 9;
        int height = 13;
        long map[] = {
            0x00ffffffff2b5500L, 0xff2b8000ff80ff40L
        };
        long rle[] = {
            0x0800010107000101L, 0x0102060001010103L, 0x0101050001010203L,
            0x0102040001010303L, 0x0101030001010403L, 0x0102020001010503L,
            0x0101010001010603L, 0x0202070302010102L, 0x0101010201010303L,
            0x0102050001010203L, 0x0101060001010103L, 0x0102070001020101L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

    static Bitmap bottomRightArrow() {
        Bitmap bitmap;
        int width = 9;
        int height = 13;
        long map[] = {
            0xff2b550000ffffffL, 0xff2b8000ff80ff40L
        };
        long rle[] = {
            0x0100080101020100L, 0x0701010001030100L, 0x0601010202030100L,
            0x0501010003030100L, 0x0401010204030100L, 0x0301010005030100L,
            0x0201010206030100L, 0x0101010007030202L, 0x0303010001020100L,
            0x0102020002030100L, 0x0501010201030100L, 0x0601010001020701L
        };

        bitmap = bitmapForEncoding(width, height, rle, map);

        return bitmap;
    }

}


