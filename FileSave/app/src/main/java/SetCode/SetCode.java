package SetCode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class SetCode {



    public static boolean getBackUpCode( InputStream inputStream,
    OutputStream outputStream) {
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        try {
            line = bufferedReader.readLine();
            while (!("[BackUp DeviceProfile]").equals(line)) {
                line=bufferedReader.readLine();
                if (line == null) {
                    line=bufferedReader.readLine();
                    if (line == null) {
                        line=bufferedReader.readLine();
                        if (line == null) {
                            line=bufferedReader.readLine();
                            if (line == null) {
                                break;
                            }
                        }
                    }
                }
            }
            while (null != line&& !line.contains("[UserCustom DeviceProfile]")) {
                outputStream.write((line+"\r\n").getBytes());
                line=bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static void getProfileCode(InputStream inputStream,
                                      OutputStream outputStream,
                                      OutputStream canChangeCode) {
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;
        try {
            line = bufferedReader.readLine();
            while (!("[UserCustom DeviceProfile]").equals(line)) {
                line=bufferedReader.readLine();
                if (line == null) {
                    line=bufferedReader.readLine();
                    if (line == null) {
                        line=bufferedReader.readLine();
                        if (line == null) {
                            line=bufferedReader.readLine();
                            if (line == null) {
                                break;
                            }
                        }
                    }
                }
            }
            if (line != null) {
                outputStream.write("[UserCustom DeviceProfile]\r\n".getBytes());
            }
            while (null != (line=bufferedReader.readLine())&& !line.equals("[BackUp DeviceProfile]")) {
                if (canChang(line)) { canChangeCode.write((line+"\r\n").getBytes()); }else{
                    outputStream.write((line+"\r\n").getBytes());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean canChang(String line) {
        return isFPS(line) || isAnti(line) || isScaleFactor(line)
                || isQualitySetting(line) || isHDRSetting(line);
    }
    //帧数
    public static boolean isFPS(String line) {
        return line.startsWith("+CVars=0B57292C3B3E3D1C0F101A1C3F292A");
    }
    //画质设置
    public static boolean isQualitySetting(String line) {
        return line.startsWith("+CVars=0B572C0A1C0B280C1815100D002A1C0D0D10171E");
    }
    //抗锯齿
    public static boolean isAnti(String line) {
        return line.startsWith("+CVars=0B572C0A1C0B342A38382A1C0D0D10171E44");
    }
    //画面风格
    public static boolean isHDRSetting(String line) {
        return line.startsWith("+CVars=0B572C0A1C0B313D2B2A1C0D0D10171E44");
    }

    //分辨率
    public static boolean isScaleFactor(String line) {
        return line.startsWith("+CVars=0B5734161B10151C3A16170D1C170D2A1A18151C3F181A0D160B44");
    }
}
