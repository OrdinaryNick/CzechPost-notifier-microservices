package cz.novros.cp.web.jms;

import java.util.Collection;
import java.util.Date;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import cz.novros.cp.jms.CommonConstants;
import cz.novros.cp.jms.QueueNames;
import cz.novros.cp.jms.entity.Parcel;
import cz.novros.cp.jms.message.AbstractJmsMessage;
import cz.novros.cp.jms.message.parcel.ParcelsMessage;
import cz.novros.cp.jms.message.parcel.ReadParcelsMessage;
import cz.novros.cp.jms.message.parcel.TrackingNumbersMessage;
import cz.novros.cp.jms.message.reponse.BooleanResponseMessage;
import cz.novros.cp.jms.message.user.AddTrackingNumbersMessage;
import cz.novros.cp.jms.message.user.LoginUserMessage;
import cz.novros.cp.jms.message.user.ReadTrackingNumbersMessage;
import cz.novros.cp.jms.message.user.RegisterUserMessage;
import cz.novros.cp.jms.message.user.UserMessage;
import cz.novros.cp.jms.service.AbstractJmsService;
import cz.novros.cp.web.service.ParcelService;
import cz.novros.cp.web.service.UserService;

@Service
@Profile("jms")
@Slf4j
public class JmsService extends AbstractJmsService implements ParcelService, UserService {

	@Autowired
	protected JmsService(@Nonnull final JmsTemplate jmsTemplate) {
		super(jmsTemplate);
	}

	public boolean loginUser(@Nonnull final String username, @Nonnull final String password) {
		log.debug("Trying to login user({}).", username);

		final LoginUserMessage message = new LoginUserMessage();
		fillBasicInfo(message, username);
		message.setPassword(password);

		jmsTemplate.convertAndSend(QueueNames.DATABASE_USER_QUEUE, message);
		final BooleanResponseMessage booleanResponseMessage = (BooleanResponseMessage) jmsTemplate.receiveSelectedAndConvert(message.getSenderQueue(), CommonConstants.getResponseSelector(message.getMessageId()));

		log.debug("User({}) was " + (booleanResponseMessage.isOk() ? "not " : "") + "logged.");

		return booleanResponseMessage.isOk();
	}

	public boolean registerUser(@Nonnull final String username, @Nonnull final String password) {
		log.debug("Trying to register user({}).", username);

		final RegisterUserMessage message = new RegisterUserMessage();
		fillBasicInfo(message, username);
		message.setPassword(password);

		jmsTemplate.convertAndSend(QueueNames.DATABASE_USER_QUEUE, message);
		final BooleanResponseMessage booleanResponseMessage = (BooleanResponseMessage) jmsTemplate.receiveSelectedAndConvert(message.getSenderQueue(), CommonConstants.getResponseSelector(message.getMessageId()));

		log.debug("User({}) was " + (booleanResponseMessage.isOk() ? "not " : "") + "registered.");

		return booleanResponseMessage.isOk();
	}

	@Nonnull
	public Collection<String> addTrackingNumbers(@Nonnull final String username, @Nonnull final Collection<String> trackingNumbers) {
		log.debug("Adding tracking numbers to user({}).", username);

		final AddTrackingNumbersMessage message = new AddTrackingNumbersMessage();
		fillBasicInfo(message, username);
		message.setTrackingNumbers(trackingNumbers);

		jmsTemplate.convertAndSend(QueueNames.DATABASE_USER_QUEUE, message);
		final BooleanResponseMessage booleanResponseMessage = (BooleanResponseMessage) jmsTemplate.receiveSelectedAndConvert(message.getSenderQueue(), CommonConstants.getResponseSelector(message.getMessageId()));

		log.debug("Tracking numbers to user({}) were " + (booleanResponseMessage.isOk() ? "not " : "") + "added.");

		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public Collection<String> removeTrackingNumbers(@Nonnull final String username, @Nonnull final Collection<String> trackingNumbers) {
		// TODO Implement
		return null;
	}

	@Override
	public Collection<String> readTrackingNumbers(@Nonnull final String username) {
		return null;
	}

	public Collection<String> readAllTrackingNumbers(@Nonnull final String username) {
		log.debug("Reading all tracking numbers for user({}).", username);

		final ReadTrackingNumbersMessage message = new ReadTrackingNumbersMessage();
		fillBasicInfo(message, username);

		jmsTemplate.convertAndSend(QueueNames.DATABASE_USER_QUEUE, message);
		final TrackingNumbersMessage trackingNumbersMessage = (TrackingNumbersMessage) jmsTemplate.receiveSelectedAndConvert(message.getSenderQueue(), CommonConstants.getResponseSelector(message.getMessageId()));

		log.debug("Read tracking numbers (size={}) for user({}).", trackingNumbersMessage.getTrackingNumbers().size(), username);

		return trackingNumbersMessage.getTrackingNumbers();
	}

	@Nonnull
	@Override
	public Collection<Parcel> readParcels(@Nonnull final Collection<String> trackingNumbers) {
		log.debug("Reading parcels for tracking numbers({}).", trackingNumbers);

		final ReadParcelsMessage readParcelsMessage = new ReadParcelsMessage();
		fillBasicInfo(readParcelsMessage, "tracking");
		readParcelsMessage.setTrackingNumbers(trackingNumbers);

		jmsTemplate.convertAndSend(QueueNames.DATABASE_PARCEL_QUEUE, readParcelsMessage);
		final ParcelsMessage parcelsMessage = (ParcelsMessage) jmsTemplate.receiveSelectedAndConvert(readParcelsMessage.getSenderQueue(), readParcelsMessage.getMessageId());

		log.debug("Parcels(size={}) for tracking numbers({}) were read.", parcelsMessage.getParcels().size(), trackingNumbers);

		return parcelsMessage.getParcels();
	}

	@Nonnull
	@Override
	public Collection<Parcel> refreshParcels(@Nonnull final Collection<String> trackingNumbers) {
		// TODO Call Rest CP client
		return null;
	}

	private static void fillBasicInfo(@Nonnull final UserMessage message, @Nonnull final String username) {
		message.setUsername(username);
		fillBasicInfo((AbstractJmsMessage) message, username);
	}

	private static void fillBasicInfo(@Nonnull final AbstractJmsMessage message, @Nonnull final String username) {
		message.setSenderQueue(QueueNames.WEB_QUEUE);
		message.setMessageId(username + new Date().getTime());
	}

}
