package ivory.integration;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

public class IntegrationUtils {
  public static final String D_JT = "-Dmapred.job.tracker=bespin00.umiacs.umd.edu:8021";
  public static final String D_NN = "-Dfs.default.name=hdfs://bespin00.umiacs.umd.edu:8020";

  public static final String D_JT_LOCAL = "-D mapred.job.tracker=local";
  public static final String D_NN_LOCAL = "-D fs.default.name=file:///";
   
  public static String getJar(String path, final String prefix) {
      File[] arr = new File(path).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(prefix) && !name.contains("javadoc") && !name.contains("sources");
      }
    });

    assertTrue(arr.length == 1);
    return arr[0].getAbsolutePath();
  }

  public static Configuration getBespinConfiguration() {
    Configuration conf = new Configuration();

    conf.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
    conf.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"));
    conf.addResource(new Path("/etc/hadoop/conf/mapred-site.xml"));
    conf.addResource(new Path("/etc/hadoop/conf/yarn-site.xml"));

    conf.reloadConfiguration();

    return conf;
  }

  // How to properly shell out: http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
  public static int exec(String cmd) throws IOException, InterruptedException {
    System.out.println("Executing command: " + cmd);

    Runtime rt = Runtime.getRuntime();
    Process proc = rt.exec(cmd);

    StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "STDERR");
    StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "STDOUT");

    errorGobbler.start();
    outputGobbler.start();

    int exitVal = proc.waitFor();
    System.out.println("ExitValue: " + exitVal);
    return exitVal;
  }

  private static class StreamGobbler extends Thread {
    InputStream is;
    String type;

    StreamGobbler(InputStream is, String type) {
      this.is = is;
      this.type = type;
    }

    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null)
          System.out.println(type + ">" + line);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  }
}
