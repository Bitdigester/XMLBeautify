/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package xmlbeautify;
/**
 * 
 *  This program makes raw XML files human readable by reformatting
 *  them with CR LF  and tab characters.
 * 
 *  It is implemented as a stream process in case it can be used for
 *  on-the-fly reformatting of serial instances of XML files like in
 *  web downloads
 * 
 * @author H. Taylor
 * 
 **/
import java.io.*;
import java.io.IOException;
//import mydebugtools.MyDebugTools;
/**
 *
 * @author Lenovo
 */
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
//import static xmlbeautify.Tag.type.NULL;
import static xmlbeautify.Element.State.LEFT_BRACKET;
import static xmlbeautify.Element.State.CLOSING_TAG;
import static xmlbeautify.Element.State.DATA_TEXT;
import static xmlbeautify.Element.State.NO_DATA;
import static xmlbeautify.Element.State.NULL_STATE;
import static xmlbeautify.Element.State.DATA_ATTRIBUTE;
import static xmlbeautify.Element.State.CLOSING_ATTRIBUTE;
import static xmlbeautify.Element.State.UNKNOWN;

class Element {
   int indent;
   enum State {NULL_STATE,
                UNKNOWN,
                LEFT_BRACKET, 
                CLOSING_TAG, 
                RIGHT_BRACKET, 
                DATA_TEXT,
                DATA_ATTRIBUTE,
                CLOSING_ATTRIBUTE,
                NO_DATA}
   State state;
   FileInputStream in = null;
   FileOutputStream out = null;
   StringBuilder TagName = new StringBuilder("");
   StringBuilder TagBuf = new StringBuilder("");
   int bufferedc;
   int LinesProcessed = 0;
   int Histogram = 0;
   char[] PutBuf = new char[2000];
   
   // This is the constructor of the class Element
   public Element(String InputFile, String OutputFile){
      int i;
      this.indent = 0;
      this.in = null;
      this.out = null;
      this.state = NULL_STATE;
      boolean DEBUG = FALSE; 
      
      
       if (InputFile.equals("default")) {
          try {
            this.out = new FileOutputStream("c:\\netstatNEW.txt");
            this.in = new FileInputStream("c:\\2014-07-28-17-21-54(reformatted).tcx");
 //      this.in = new FileInputStream("c:\\2014-07-28-17-21-54(short).tcx");
 //      this.in = new FileInputStream("c:\\nonexist");
 //      this.in = new FileInputStream("c:\\SLP_Actions_PersistedActionData.xml");
 //      this.out = new FileOutputStream("c:\\netstatSLP.txt");
      } catch(FileNotFoundException e){
          System.out.println("File not found \n" + e);
      }   
       } else
       try {
         this.out = new FileOutputStream(InputFile);
         this.in = new FileInputStream(OutputFile);
      } catch(FileNotFoundException e){
          System.out.println("File not found \n" + e);
      }   
     
        
    }
   
   /*
     This method outputs to the new file a formatted line of XML
   */
   public int put() {
       int i;
       try {          
           for (i=0; i < TagBuf.length(); i++)
               out.write(PutBuf[i]);               
       }      
       catch (IOException e) {
               System.out.println("Error" + e);
               }
       
       finally {
            return 0;
       }
       
   }
   /* 
      this method reads raw XML from the input stream and builds a re-formatted line
      of XML in TagBuf 
   */
   public int get() {
       int i;
       int c, returnValue = 0;
       TagBuf.setLength(0);
       boolean DEBUG = FALSE; 
       boolean LeadingTab = FALSE;
       boolean EscapeXML = FALSE;
      
       try {
          while (TRUE) {
              if (state == NO_DATA) {
                  state = NULL_STATE;
                  c = bufferedc;  
              } else {
                  c = in.read();
              }
              if (c == -1) {
                  returnValue = -1;
                  return -1;
              }
              if (EscapeXML == TRUE){
                  /* do nothing except transfer the char   */
                  /* to the output stream                  */
                  TagBuf.append((char)c);
                  /*  check for escape termination char    */
                  if (c == '"') EscapeXML = FALSE;
                  continue;
              }
              if ((c == '"') && (EscapeXML == FALSE)){
                    EscapeXML = TRUE;
                    TagBuf.append((char)c);
                    continue;
                }
              
              /* Check for XML control characters           */
              if ((c == '<') && (state == NULL_STATE)) {
                /* Found the start of a <tag>    */
                              
                state = LEFT_BRACKET;
                TagName.setLength(0); // Initialize data buffers
                TagBuf.setLength(0);
                /* First write out a leading tab since we assume  */
                /* this is not an element closing </tag>          */
                /* (This will be rescinded later if it is.)       */
                TagBuf.append('\t');
                LeadingTab = TRUE;
                TagBuf.append((char)c);
                if (DEBUG) System.out.format("%c %d LEFT_BRACKET\n", (char)c, indent);
                continue;
              }
              
              if ((c == '/') && (state == LEFT_BRACKET)){ 
                state = CLOSING_TAG;               
                TagBuf.append((char)c);
                if (DEBUG) System.out.format("%c %d CLOSING_TAG_FOUND\n", (char)c, indent);
                continue;
              }
              if ((c == '/') && (state == DATA_ATTRIBUTE)){ 
                state = CLOSING_ATTRIBUTE;               
                TagBuf.append((char)c);
                if (DEBUG) System.out.format("%c %d CLOSING_ATTRIBUTE_FOUND\n", (char)c, indent);
                continue;
              }
              /* End of <tag> name text of type                         */
              /*    <Tagname Id="abcdefg" />            */
              if ((c == ' ') && (state == LEFT_BRACKET)) { 
              /* this is a TITLE_ATTRIBUTE type <tag> */
              /* like <Activity Sport="Biking">       */
                indent++;  // indent one tab more
                
                state = DATA_ATTRIBUTE;
                TagBuf.append((char)c);                

                if (DEBUG) System.out.format("%c %d LEFT_BRACKET+SPACE->DATA_ATTRIBUTE\n", (char)c, indent);
                if (DEBUG) System.out.println("Name = " + TagName.toString());
                continue;
              }
              
              if ((c == '>') && (state == LEFT_BRACKET)){
                  /* End of <tag> of type                         */
                  /* <Activity>  so it's a new element name       */
                state = UNKNOWN;
                /* increase the indentation tab count             */
                indent++;
                TagBuf.append((char)c);
                if (DEBUG) System.out.format("%c %d LEFT_BRACKET->UNKNOWN\n", (char) c, indent);                
                continue;                          
              }
              
              if ((c == '>') && (state == CLOSING_TAG)){ 
               /* End of tag of type */
               /* </Tagname> */             
                state = NULL_STATE;
                TagBuf.append((char)c);
                
                /* delete the leading tab   */
                TagBuf.deleteCharAt(0);
                /* reduce the indentation count  */
                indent--;
                
                /* Write out formatting chars     */
                TagBuf.append((char)'\r');
                TagBuf.append((char)'\n');
                for (i = 0; i != indent; i++) {                              
                       TagBuf.append((char)'\t');  /* found a matching title tag */                        
                    } 

               if (DEBUG) System.out.format("%c %d CLOSING_TAG->NULL_STATE\n", c, indent);             
               if (DEBUG) System.out.println("Name = " + TagName.toString());
               LinesProcessed++;
               return 0;
               }
              
              if ((c == '>') && (state == CLOSING_ATTRIBUTE)){ 
               /* End of tag of type */
               /* </TotalTimeSeconds> */             
                state = NULL_STATE;
                TagBuf.append((char)c);
                /* reduce the indentation count  */
                indent--;
                
                /* Write out formatting chars     */
                TagBuf.append((char)'\r');
                TagBuf.append((char)'\n');
                for (i = 0; i != indent; i++) {                              
                       TagBuf.append((char)'\t');  /* found a matching title tag */                        
                    } 
               LinesProcessed++;
               if (DEBUG) System.out.format("%c %d CLOSING_TAG->NULL_STATE\n", c, indent);             
               if (DEBUG) System.out.println("Name = " + TagName.toString());             
               return 0;
               }
                  
               if ((c == '>') && (state == DATA_ATTRIBUTE)) { 
                  /* End of tag of type        */
                  /* <Activity Sport="Biking"> */
                
                    state = NULL_STATE;
                    TagBuf.append((char)c);
                    
                    TagBuf.append((char)'\r');
                    TagBuf.append((char)'\n');
                    /* add required number of tab characters */
                    for (i=0; i != indent; i++)                               
                        TagBuf.append((char)'\t');
                    LinesProcessed++;
                    if (DEBUG) System.out.format("%c %d DATA_ATTRIBUTE->NULL_STATE\n", (char) c, indent); 
                    if (DEBUG) System.out.println("Name = " + TagName.toString());                    
                    return 0;
                   
               }   
               if ((c == '<') && (state == DATA_TEXT)){
                 /* start of a new tag when expecting data */
                 /* like <Activity><Jumping>    or         */
                   /* <Time>2014-07-28T17:21:55Z<???       */
                 state = NO_DATA;
                 if (DEBUG) System.out.format("%c %d DATA_TEXT-> NO_DATA\n", (char)c, indent);
                 /* special state where we buffer the '<' */
                 bufferedc = c; 
                 
                 returnValue = 0;
                 return 0;
               } 
                if ((c == '<') && (state == UNKNOWN)){
                 /* start of a new tag when state unknown */
                 /* We have encountered continguous tags  */
                 /* like <Activity><Activities> 
                    i.e. the <tag> contains no data only tag name text */
                 state = NO_DATA;
                 
                 /* special state where we buffer the '<' */
                 bufferedc = c;                 
                 /* write out formatting chars for lext line */            
                 TagBuf.append((char)'\r');
                 TagBuf.append((char)'\n');
                 for (i=0; i != indent; i++) {                              
                    TagBuf.append((char)'\t'); 
                 }
                 LinesProcessed++;
                 if (DEBUG) System.out.format("%c %d UNKNOWN-> NO_DATA\n", (char)c, indent);
                 returnValue = 0;                
                 return 0;
                }
                /* this is not an XML control character   */
                /* find out what to do with it            */              
                if ((state == LEFT_BRACKET) || (state == CLOSING_TAG)) {
                    /* It's <tag> name text               */
                    /* either <Activity> or </Activity>   */
//                if (DEBUG) System.out.format("%c TAG NAME TEXT\n",c);
                
                    TagBuf.append((char)c);
                    TagName.append((char)c); 
                    continue;               
               } 
               if (state == DATA_ATTRIBUTE) { 
//                    if (DEBUG) System.out.format("%c ATTRIBUTE TEXT\n",c);
                   /* It's <tag> attribute text      */
                   /* like <Activity Sport="Biking"> */   
                    TagBuf.append((char)c);
                    continue;               
               }                 
               
               if (state == UNKNOWN){
                /* we now know this is data text    */
                  state = DATA_TEXT;
 //                 if (DEBUG) System.out.format("%c %d UNKNOWN->DATA_TEXT\n", (char)c, indent);                 
                  TagBuf.append((char)c);
                  continue; 
                }
               if (state == DATA_TEXT){
                /* this is data text    */
 //                 if (DEBUG) System.out.format("%c DATA_TEXT\n", (char)c);                 
                  TagBuf.append((char)c);
                  continue; 
                }
               if (state == NULL_STATE){
                /* this is an unknown character probably LF or formatting    */
//                  if (DEBUG) System.out.format("%c UNKNOWN TEXT!\n", (char)c);                 
                  TagBuf.append((char)c);
                  continue; 
                }            
    } 
       return -1; 
       
    } 
       catch (IOException e) {
               System.out.println("Error" + e);
               }
       finally {
            if (returnValue == -1) return returnValue;
            TagBuf.getChars(0, TagBuf.length(), PutBuf, 0);
 //           if (DEBUG) printlnSp(PutBuf, TagBuf.length());
            if (Histogram++ == 5000){
                Histogram = 0;
            System.out.printf("-\n");
        }
        return returnValue;
    }
 }
       
}

/**
 *
 * @author Lenovo
 */
public class XMLBeautify {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    if (args.length == 0) {
         System.out.format("Error: No input file specified\r\n");
         return;
      }
      if (args.length == 1) {
         System.out.format("Error: No output file specified\r\n");
         return;
      }
     
     
      Element element = new Element(args[0], args[1]);
      if (element.in == null)
          return;
      /*
        Get() raw XML element declarations and data
        then put() the formatted results to the output file
      */
      try {  
         while (TRUE) {
           if ( element.get() == -1 )
               break; else {
               element.put();
           }
         }              
      } finally {
          System.out.format("\r\n\r Done\r\n");
          System.out.format("%d LinesProcessed\r\n", element.LinesProcessed);
          return;
         }
   
}
}

    

