package ro.ase.csie.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    public void dispatch(String notificationXml) {
        log.info("[MOCK EMAIL DISPATCH]\n{}", notificationXml);
    }
}
