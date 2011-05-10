package org.jboss.arquillian.container.tomcat.remote_6;

import java.io.File;
import java.io.IOException;

//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.jboss.shrinkwrap.api.Archive;


/**
 *
 * @author Ondrej Zizka
 */
public class HttpUploader {
   
    // Try: curl --upload-file <path to warfile> "http://<tomcat username>:<tomcat password>@<hostname>:<port>/manager/deploy?path=/<context>&update=true"

   
    public static void main(String[] args) throws IOException {
    }
    
    void upload( TomcatRemoteConfiguration conf, String context, Archive archive )
    {
       /*
        HttpClient client = new HttpClient();
        MultipartPostMethod mPost = new MultipartPostMethod(url);
        client.setConnectionTimeout(8000);

        // Send any XML file as the body of the POST request
        File f1 = new File("students.xml");
        File f2 = new File("academy.xml");
        File f3 = new File("academyRules.xml");

        System.out.println("File1 Length = " + f1.length());
        System.out.println("File2 Length = " + f2.length());
        System.out.println("File3 Length = " + f3.length());

        mPost.addParameter(f1.getName(), f1);
        mPost.addParameter(f2.getName(), f2);
        mPost.addParameter(f3.getName(), f3);

        int statusCode1 = client.executeMethod(mPost);

        System.out.println("statusLine>>>" + mPost.getStatusLine());
        mPost.releaseConnection();
        */
    }
    
/*    
      HttpClient client = new DefaultHttpClient();
      String uri = String.format("http://%s:%s@%s:%d/manager/deploy?path=/%s&update=true",
              this.conf.getUser(), this.conf.getPass(), this.conf.getHost(), StringUtils.substringBefore(archive.getName(), ".")  );
      client.execute( new HttpPost(this), )
*/
      
}// class
