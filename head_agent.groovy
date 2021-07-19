
import org.arl.fjage.*
import org.arl.fjage.Message
import org.arl.fjage.RealTimePlatform
import org.arl.unet.phy.*
import org.arl.unet.mac.*
import org.arl.unet.*
import org.arl.unet.net.*
import org.arl.unet.PDU
import org.arl.unet.net.Router
import org.arl.unet.nodeinfo.NodeInfo
import org.arl.unet.localization.*
import org.arl.unet.localization.RangeNtf.*
import org.arl.unet.localization.Ranging.*
import org.arl.unet.localization.RangeReq
import org.arl.unet.net.RouteDiscoveryReq
import java.nio.ByteOrder
import org.arl.fjage.Behavior
import org.arl.unet.phy.TxFrameReq
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class head_agent extends UnetAgent {
    def phy;
    def nodeInfo;
    private final static PDU format = PDU.withFormat
    {
        uint16('addres');
        int16('loca');
        int16('loca1');
        int16('loca2');
    }
    
    private final static PDU format1 = PDU.withFormat
    {
        uint16('yesno');
        uint16('address');
    }
    
  String hashtext;
  String hashtext1;
  String hashtext2;
  
  def mp = [:] ;

  void startup() 
  {
    phy = agentForService Services.PHYSICAL;
    subscribe topic(phy);
 
    def input="300725"
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] messageDigest = md.digest(input.getBytes());
    BigInteger no = new BigInteger(1, messageDigest);
    hashtext = no.toString(16);
    while (hashtext.length() < 32) {
    hashtext = "0" + hashtext;
    }
            
    mp.put(101,hashtext);
    
    def input1="200500"
    byte[] messageDigest1 = md.digest(input1.getBytes());
    BigInteger no1 = new BigInteger(1, messageDigest1);
     hashtext1 = no1.toString(16);
    while (hashtext1.length() < 32) {
        hashtext2 = "0" + hashtext1;
    }
            
    mp.put(102,hashtext1);

    def input2="500300";
    byte[] messageDigest2 = md.digest(input2.getBytes());
    BigInteger no2 = new BigInteger(1, messageDigest2);
     hashtext2 = no2.toString(16);
    while (hashtext2.length() < 32) {
        hashtext2 = "0" + hashtext2;
    }
            
    mp.put(103,hashtext2);
  }

 
    void processMessage(Message msg) 
    {

        if(msg instanceof DatagramNtf && msg.protocol == Protocol.USER )
        {
            def rx = format.decode(msg.data);
            int addr=rx.addres;
            int sl0=rx.loca ;
            int sl1=rx.loca1 ;
            int sl2=rx.loca2 ;
            
            String str1=sl0.toString();
            String str2=sl1.toString();

            def fstr=str1+str2;
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] messageDigest3 = md.digest(fstr.getBytes());
            BigInteger no3 = new BigInteger(1, messageDigest3);
            String hashtext3 = no3.toString(16);
            while (hashtext3.length() < 32) {
                hashtext3 = "0" + hashtext3;
            }
         
            int yesno=0;
            
            println "hash    "+hashtext;
            println "hash1   "+hashtext1;
            println "hash2   "+hashtext2;
            println "address "+addr;
            println "hash3   "+hashtext3;
                
            if(hashtext3==mp[addr])
                yesno=1;
            
            def datapacket = format1.encode(address:addr, yesno: yesno );

            phy << new DatagramReq(to: 107, protocol: Protocol.USER, data:datapacket );
        }
    
    }
}