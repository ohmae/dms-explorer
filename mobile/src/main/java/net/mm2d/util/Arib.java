/*
 * Copyright (c) 2016 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.util;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介(OHMAE Ryosuke)</a>
 */
public class Arib {
    public static String toDisplayableString(String string) {
        final StringBuilder sb = new StringBuilder();
        final char[] array = string.toCharArray();
        for (final char c : array) {
            switch (c) {
                case 0xE0F8:
                    sb.append("[HV]");
                    break;
                case 0xE0F9:
                    sb.append("[SD]");
                    break;
                case 0xE0FA:
                    sb.append("[P]");
                    break;
                case 0xE0FB:
                    sb.append("[W]");
                    break;
                case 0xE0FC:
                    sb.append("[MV]");
                    break;
                case 0xE0FD:
                    sb.append("[手]");
                    break;
                case 0xE0FE:
                    sb.append("[字]");
                    break;
                case 0xE0FF:
                    sb.append("[双]");
                    break;
                case 0xE180:
                    sb.append("[デ]");
                    break;
                case 0xE181:
                    sb.append("[S]");
                    break;
                case 0xE182:
                    sb.append("[二]");
                    break;
                case 0xE183:
                    sb.append("[多]");
                    break;
                case 0xE184:
                    sb.append("[解]");
                    break;
                case 0xE185:
                    sb.append("[SS]");
                    break;
                case 0xE186:
                    sb.append("[B]");
                    break;
                case 0xE187:
                    sb.append("[N]");
                    break;
                case 0xE188:
                    sb.append("■");
                    break;
                case 0xE189:
                    sb.append("●");
                    break;
                case 0xE18A:
                    sb.append("[天]");
                    break;
                case 0xE18B:
                    sb.append("[交]");
                    break;
                case 0xE18C:
                    sb.append("[映]");
                    break;
                case 0xE18D:
                    sb.append("[無]");
                    break;
                case 0xE18E:
                    sb.append("[料]");
                    break;
                case 0xE18F:
                    sb.append("[鍵]");
                    break;
                case 0xE190:
                    sb.append("[前]");
                    break;
                case 0xE191:
                    sb.append("[後]");
                    break;
                case 0xE192:
                    sb.append("[再]");
                    break;
                case 0xE193:
                    sb.append("[新]");
                    break;
                case 0xE194:
                    sb.append("[初]");
                    break;
                case 0xE195:
                    sb.append("[終]");
                    break;
                case 0xE196:
                    sb.append("[生]");
                    break;
                case 0xE197:
                    sb.append("[販]");
                    break;
                case 0xE198:
                    sb.append("[声]");
                    break;
                case 0xE199:
                    sb.append("[吹]");
                    break;
                case 0xE19A:
                    sb.append("[PPV]");
                    break;
                case 0xE19B:
                    sb.append("[秘]");
                    break;
                case 0xE19C:
                    sb.append("[ほか]");
                    break;
                case 0xE080: // 㐂
                    sb.append((char) 0x3402);
                    break;
                case 0xE082: // 㔟
                    sb.append((char) 0x351F);
                    break;
                case 0xE083: // 詹
                    sb.append((char) 0x8A79);
                    break;
                case 0xE085: // 﨑
                    sb.append((char) 0xFA11);
                    break;
                case 0xE086: // 㟢
                    sb.append((char) 0x37E2);
                    break;
                case 0xE08F: // 㻚
                    sb.append((char) 0x3EDA);
                    break;
                case 0xE090: // 䂓
                    sb.append((char) 0x4093);
                    break;
                case 0xE094: // 䉤
                    sb.append((char) 0x4264);
                    break;
                case 0xE2F0: // ⁉
                    sb.append((char) 0x2049);
                    break;
                case 0xE2FF: // ㉑
                    sb.append((char) 0x3251);
                    break;
                case 0xE380: // ㉒
                    sb.append((char) 0x3252);
                    break;
                case 0xE381: // ㉓
                    sb.append((char) 0x3253);
                    break;
                case 0xE382: // ㉔
                    sb.append((char) 0x3254);
                    break;
                case 0xE39D: // ㉕
                    sb.append((char) 0x3255);
                    break;
                case 0xE39E: // ㉖
                    sb.append((char) 0x3256);
                    break;
                case 0xE39F: // ㉗
                    sb.append((char) 0x3257);
                    break;
                case 0xE3A0: // ㉘
                    sb.append((char) 0x3258);
                    break;
                case 0xE3A1: // ㉙
                    sb.append((char) 0x3259);
                    break;
                case 0xE3A2: // ㉚
                    sb.append((char) 0x325A);
                    break;
                case 0xE3A3: // ⓫
                    sb.append((char) 0x24EB);
                    break;
                case 0xE3A4: // ⓬
                    sb.append((char) 0x24EC);
                    break;
                case 0xE3A5: // ㉛
                    sb.append((char) 0x325B);
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}
