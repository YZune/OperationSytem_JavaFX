import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ManagerUtils {

    public static String readConsole(String cmd, Boolean isPrettify) throws IOException {
        StringBuilder cmdOut = new StringBuilder();
        Process process = Runtime.getRuntime().exec(cmd);
        InputStream fis = process.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("GB2312")));
        String line;

        if (isPrettify == null || isPrettify) {
            while ((line = br.readLine()) != null) {
                cmdOut.append(line);
            }
        } else {
            while ((line = br.readLine()) != null) {
                cmdOut.append(line).append(System.getProperty("line.separator"));
            }
        }

        return cmdOut.toString().trim();
    }

    public static Map getProcessList() {
        int tCount;
        int count = 0;
        BufferedReader br = null;
        ProcessInfoBean pInfo;
        HashMap<Integer, ProcessInfoBean> map = new HashMap<>();

        String name;
        String pid;
        String sessionName;
        String sessionNum;
        String memory;

        String line;
        StringTokenizer st;

        try {
            Process proc = Runtime.getRuntime().exec("tasklist");
            br = new BufferedReader(new InputStreamReader(proc.getInputStream(), Charset.forName("GB2312")));

            System.out.println(br.readLine());
            System.out.println(br.readLine());
            System.out.println(br.readLine());

            while ((line = br.readLine()) != null) {
                System.out.println(line);
                st = new StringTokenizer(line, " ");
                while (st.hasMoreElements()) {
                    tCount = st.countTokens();
                    if (tCount == 6) {
                        name = st.nextToken();
                        pid = st.nextToken();
                        sessionName = st.nextToken();
                        sessionNum = st.nextToken();
                        memory = st.nextToken();
                        pInfo = new ProcessInfoBean(name, pid, sessionName, sessionNum, memory);
                        map.put(++count, pInfo);
                    } else if (tCount == 8) {
                        name = st.nextToken() + " " + st.nextToken() + " " + st.nextToken();
                        pid = st.nextToken();
                        sessionName = st.nextToken();
                        sessionNum = st.nextToken();
                        memory = st.nextToken();
                        pInfo = new ProcessInfoBean(name, pid, sessionName, sessionNum, memory);
                        map.put(++count, pInfo);
                    } else {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return map;
    }

}
