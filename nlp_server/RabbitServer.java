import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer; 
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP.BasicProperties;

import com.efficientmail.SentiClassifyLoader;
import com.efficientmail.SentiClassify;
import com.efficientmail.ThemeDetect;
import com.efficientmail.TextClean;

//import com.efficientmail.Meter;

import org.apache.commons.daemon.*;

import java.util.Map;
import java.util.ArrayList;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;

public class RabbitServer implements Daemon {
	private static final String RPC_QUEUE_NAME = "rpc_queue";
	private static final String REPORT_DIR	   = "/var/www/reports";

	private Connection connection = null;
	private Channel channel = null;	
	private SentiClassifyLoader ldr = null;

	@Override
	public void init(DaemonContext dc) throws DaemonInitException, Exception {
		System.out.println("initializing ...");
	}

	@Override
	public void stop() throws Exception {
		this.channel.close();
		this.connection.close();
		
		System.out.println("stopping ...");
	}

	@Override
	public void destroy() {
		System.out.println("done.");
	}

  	@Override
    public void start() throws Exception, IOException, InterruptedException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		this.connection = factory.newConnection();

		this.ldr = new SentiClassifyLoader(); 

		this.channel	= this.connection.createChannel();
		this.channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
		this.channel.basicQos(200);

		QueueingConsumer consumer = new QueueingConsumer(this.channel);
		this.channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

		System.out.println(" [x] Awaiting RPC requests");

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();

			String message = new String(delivery.getBody());

			int first = message.indexOf(":",0);
			int secd  = message.indexOf(":",first+1);
			int third = message.indexOf(":",secd+1);

			String lkey   = message.substring(0,first);
			String agent  = message.substring(first+1,secd);
			String file   = message.substring(secd+1,third);

			String hdr    = lkey + ":" + agent + ":" + file + ":";
			message 	  = message.replace(hdr,"");

			String response   = this.getSentiment(message,lkey);

			String excel_path = REPORT_DIR + File.separator + lkey + File.separator + file;
/*
			if ("demo".equals(lkey)) {
				BasicProperties props 	   = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties().builder().correlationId(props.getCorrelationId()).build();

				this.channel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes());
			}
*/
			this.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

			if ("EOF".equals(message)) {
				File file1= new File(excel_path + ".part");
				File file2= new File(excel_path);
				file1.renameTo(file2);
			}
			else {
				try {
					String fields[] = response.split(";");

					System.out.println(response);

					message = agent + "\t" + message + "\t" + fields[0] + "\t" + fields[1] + "\t" + fields[2] + "\t" + fields[3] + "\t" + fields[4] + "\t" + fields[5] + "\n";

					FileOutputStream fd = new FileOutputStream(excel_path + ".part", true);
					fd.write(message.getBytes());
					fd.close();
				}
				catch (Exception e) {

				}
			}
		}
	}

	public String getSentiment(String msg, String licence_code) {
		SentiClassify s	  = new SentiClassify(this.ldr);
		ThemeDetect   t	  = new ThemeDetect(this.ldr);

		if (true) {
			msg = msg.replaceAll("\\r\\n|\\r|\\n", " ");

			System.out.println(licence_code + " " + msg);

			float scr[] = s.detectSentiment(msg);
			Map<String,ArrayList<String>> themes_map = t.detectThemes(msg);

			String themes_pos = " ";
			String themes_neg = " ";
			String theme_kw   = " ";

			for (String theme : themes_map.get("pos"))
				themes_pos += theme + ",";

			for (String theme : themes_map.get("neg"))
				themes_neg += theme + ",";

			for (String theme : themes_map.get("kw"))
				theme_kw += theme + ",";

			float base 	= scr[0] + scr[1];

			int pos_pc = 0;
			int neg_pc = 0;

			pos_pc  = Math.round(scr[0]/base*100);
			neg_pc  = Math.round(scr[1]/base*100);

			return String.valueOf(base) + ";" + Integer.toString(pos_pc) + ";" + Integer.toString(neg_pc) + ";" + themes_pos + ";" + themes_neg + ";" + theme_kw; 
		}
		else {
			return "Licence expired or key not provided"; 
		}
	}
}
