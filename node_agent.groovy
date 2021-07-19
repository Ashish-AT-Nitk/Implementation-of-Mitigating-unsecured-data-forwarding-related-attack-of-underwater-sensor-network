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
import org.arl.unet.localization.RangeReq
import org.arl.unet.net.RouteDiscoveryReq
import groovy.time.TimeCategory 
import groovy.time.TimeDuration
import java.nio.ByteOrder

class node_agent extends UnetAgent {
    float neighbor_addr;
    def nodeInfo;
    def sLocation;
    def router;
    def sdis=1000000;
    def sadd=0;
    def actual=[:];
    def nos=0;

  private final static PDU format = PDU.withFormat
  {
    uint16('addres');
    uint16('loca');
    int16('loca1');
    int16('loca2');
  }
  
    private final static PDU format1 = PDU.withFormat
  {
    uint16('yesno');
    uint16('address');
  }

  void startup() 
  {
    def phy = agentForService Services.PHYSICAL;
    subscribe topic(phy);
    
    router = agentForService Services.ROUTING;
    subscribe topic(router);
    
    nodeInfo = agentForService Services.NODE_INFO;
    sLocation=nodeInfo.location;
    
    println 'Starting discovery...';
  
    phy << new DatagramReq(to: 0, protocol:Protocol.MAC);
    
}
    
  def mp = [:] ;

  void processMessage(Message msg) 
  {

    def phy = agentForService Services.PHYSICAL;
    subscribe topic(phy);
        
    if(msg instanceof DatagramNtf && msg.protocol==Protocol.USER )
    { 
        def rx = format1.decode(msg.data);
        int temp=rx.yesno;
        int adr=rx.address;
        if(temp==1)
        {
            actual.put(adr,1);
        }
    }
    
    if(msg instanceof RxFrameNtf && msg.protocol==Protocol.MAC )
    {
        nos++;
        def rx = format.decode(msg.data);
        neighbor_addr=rx.addres;
        double sl0=rx.loca ;
        double sl1=rx.loca1 ;
        double sl2=rx.loca2 ;
        
        def datapacket = format.encode(addres: neighbor_addr, loca: rx.loca , loca1: rx.loca1 ,loca2: rx.loca2 );
        phy << new DatagramReq(to: 108, protocol:Protocol.USER, data:datapacket  );
       
        def des;
        des=Math.sqrt((sl0-sLocation[0])*(sl0-sLocation[0])+(sl1-sLocation[1])*(sl1-sLocation[1])+(sl2-sLocation[2])*(sl2-sLocation[2]));
       
        mp.put(des,(int)neighbor_addr);
  
        if(neighbor_addr==102.0)
        {
            println "Address : "+neighbor_addr;
            println "Calculated distance : "+des;
            println "actual distance : 195.0";
       
        }
        
        else if(neighbor_addr==103.0){
            println "Address : "+neighbor_addr;
            println "Calculated distance : "+des;
            println "actual distance : 533.87.0";

        }
        
        else if(neighbor_addr==101.0){
            println "Address : "+neighbor_addr;
            println "Calculated distance : "+des;
            println "actual distance : 371.01212918178294";

        }
    
        if(nos==3)
        {
            for ( e in mp ) 
            {
                if(actual.containsKey(e.value))
                {
                    sadd=e.value;
                    break;
                }
                    
            }
            
            println "Attack has been resolved"
            router << new RouteDiscoveryNtf(to: 106, nextHop: sadd, reliability: true);  
            def numb=1;
            while(numb<21)
            {
                println "packet "+numb+" sent to 106.0 via "+sadd;
                router << new DatagramReq(to: 106, data:"hi", protocol:Protocol.DATA );
                numb++;
           
            }
        }
    }
  }
}
