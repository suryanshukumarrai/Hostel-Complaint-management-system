package com.hostel.service;

import com.hostel.entity.Complaint;
import com.hostel.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChromaSyncRunner implements ApplicationRunner {

    @Value("${chroma.syncOnStartup:false}")
    private boolean syncOnStartup;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ChromaClient chromaClient;

    @Override
    public void run(ApplicationArguments args) {
        if (!syncOnStartup) {
            return;
        }

        chromaClient.ensureCollection();
        List<Complaint> complaints = complaintRepository.findAll();
        for (Complaint complaint : complaints) {
            chromaClient.upsertComplaint(complaint);
        }
    }
}
