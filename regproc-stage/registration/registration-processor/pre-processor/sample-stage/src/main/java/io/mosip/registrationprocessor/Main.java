package io.mosip.registrationprocessor;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;

import com.google.gson.Gson;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipRouter;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleAPIManager;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.constant.MappingJsonConstants;
import io.mosip.registration.processor.core.constant.ProviderStageName;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.PacketManagerException;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.packet.storage.utils.PriorityBasedPacketManagerService;
import io.mosip.registration.processor.packet.storage.utils.PacketManagerService;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ImageType;
import io.mosip.biometrics.util.Modality;

@ComponentScan(basePackages = { "${mosip.auth.adapter.impl.basepackage}",
		"io.mosip.registration.processor.core.config",
		"io.mosip.registration.processor.stages.config", "io.mosip.registration.processor.status.config",
		"io.mosip.registration.processor.rest.client.config", "io.mosip.registration.processor.packet.storage.config",
		"io.mosip.registration.processor.packet.manager.config", "io.mosip.registration.processor.core.kernel.beans" })

public class Main extends MosipVerticleAPIManager{
    private static final String STAGE_PROPERTY_PREFIX = "mosip.regproc.sample.stage.";
	public static final MessageBusAddress SAMPLE_STAGE_BUS_IN = new MessageBusAddress("sample-stage-bus-in");
    public static final MessageBusAddress SAMPLE_STAGE_BUS_OUT = new MessageBusAddress("sample-stage-bus-out");

    private static Logger regProcLogger = RegProcessorLogger.getLogger(Main.class);

    /** The cluster manager url. */
	@Value("${vertx.cluster.configuration}")
	private String clusterManagerUrl;

    @Value("${worker.pool.size}")
	private Integer workerPoolSize;

	/** After this time intervel, message should be considered as expired (In seconds). */
	@Value("${mosip.regproc.credentialrequestor.message.expiry-time-limit}")
	private Long messageExpiryTimeLimit;

    /** Mosip router for APIs */
	@Autowired
	MosipRouter router;
	@Autowired
	RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	@Autowired
	private PriorityBasedPacketManagerService basedPacketManagerService;
	
	@Autowired
	private PacketManagerService packetManagerService;


    @Override
    public MessageDTO process(MessageDTO object) {
        String regId = object.getRid();
        regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
			regId, "SampleStage::process()::" +
			new Gson().toJson(object));
        //Retrive Registration status record
        InternalRegistrationStatusDto registrationStatusDto = null;
        
		registrationStatusDto = registrationStatusService.getRegistrationStatus(
					regId, object.getReg_type(), object.getIteration(), object.getWorkflowInstanceId());
		if(registrationStatusDto != null){
            regProcLogger.info("status:regId="+ regId + ",status="+ registrationStatusDto.getStatusComment());
           
			//fetch required attributes from packet using packetmanager API

			try {
			/* 
				String individualBiometricsObject = basedPacketManagerService.getFieldByMappingJsonKey(
					regId,
					MappingJsonConstants.INDIVIDUAL_BIOMETRICS,
					registrationStatusDto.getRegistrationType(),
					 ProviderStageName.BIO_DEDUPE);
					 regProcLogger.info(individualBiometricsObject);
				
				String identityData = basedPacketManagerService.getFieldByMappingJsonKey(
						regId,
						MappingJsonConstants.EMAIL,
						registrationStatusDto.getRegistrationType(),
						 ProviderStageName.BIO_DEDUPE);
				regProcLogger.info("Address="+identityData);
			**/
			//retrive attributes from Packet via PacketManager API
				Map<String,String> tags = new HashMap<String,String>();
				if(object.getTags() != null)
					tags = object.getTags();

//				packetManagerService.addOrUpdateTags(regId, tags);

				//Map<String,String> attributes = packetManagerService.getAllFieldsByMappingJsonKeys(regId, object.getReg_type(), ProviderStageName.BIO_DEDUPE);
				//attributes.forEach( (k,v)->{
				//	regProcLogger.info("Key="+k+",Value="+v);
				//});
				BiometricRecord record = basedPacketManagerService.getBiometricsByMappingJsonKey(regId,
				MappingJsonConstants.INDIVIDUAL_BIOMETRICS, registrationStatusDto.getRegistrationType(),
				ProviderStageName.QUALITY_CHECKER);

				//BiometricRecord record = packetManagerService.getBiometrics(regId, "user", object.getReg_type(), ProviderStageName.BIO_DEDUPE);
				for(BIR bir: record.getSegments()){
					BiometricType biometricType = bir.getBdbInfo().getType().get(0);
					if(biometricType.name().equalsIgnoreCase(BiometricType.FACE.name())){
						byte[] faceData = bir.getBdb();
						
						//save face data to a file
						try (FileOutputStream fos = new FileOutputStream("face.raw")) {
							fos.write(faceData);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						String isoJpegFace = CommonUtil.convertISOImageType( new String(faceData),
								 Modality.Face, ImageType.JPEG);
						String base64Image = isoJpegFace.split(",")[1];
						byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
						BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));

						File file = new File("face-"+ ".jpg");
						ImageIO.write(img, "jpg", file);
																  
					}
					
				}
				
			} catch (ApisResourceAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PacketManagerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	
        }
		else{
			//error - invalid registration ID

		}
        return object;
    }

    @Override
    protected String getPropertyPrefix() {
        return STAGE_PROPERTY_PREFIX;
    }
    @Override
	public void start(){
		router.setRoute(this.postUrl(getVertx(), SAMPLE_STAGE_BUS_IN,SAMPLE_STAGE_BUS_OUT));
		this.createServer(router.getRouter(), getPort());
	}

    /**
	 * Deploy verticle.
	 */
	public void deployVerticle() {
		mosipEventBus = this.getEventBus(this, clusterManagerUrl, workerPoolSize);
		this.consumeAndSend(mosipEventBus, SAMPLE_STAGE_BUS_IN, 
			SAMPLE_STAGE_BUS_OUT, messageExpiryTimeLimit);
	}
}