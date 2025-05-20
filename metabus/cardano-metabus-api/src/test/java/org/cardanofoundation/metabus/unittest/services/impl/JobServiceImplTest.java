package org.cardanofoundation.metabus.unittest.services.impl;

import org.cardanofoundation.metabus.common.entities.JobJPA;
import org.cardanofoundation.metabus.common.entities.ScheduledBatchesJPA;
import org.cardanofoundation.metabus.common.enums.BatchStatus;
import org.cardanofoundation.metabus.common.offchain.BusinessData;
import org.cardanofoundation.metabus.common.offchain.Job;
import org.cardanofoundation.metabus.controllers.BaseResponse;
import org.cardanofoundation.metabus.exceptions.MetabusErrors;
import org.cardanofoundation.metabus.exceptions.MetabusException;
import org.cardanofoundation.metabus.mappers.JobMapper;
import org.cardanofoundation.metabus.repositories.JobRepository;
import org.cardanofoundation.metabus.repositories.ScheduledBatchesRepository;
import org.cardanofoundation.metabus.security.properties.MetabusSecurityProperties;
import org.cardanofoundation.metabus.security.properties.MetabusSecurityProperties.JobTypeAuthorization;
import org.cardanofoundation.metabus.services.JobProducerService;
import org.cardanofoundation.metabus.services.impl.JobServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JobServiceImplTest extends BaseUnitTest {

    JobRepository jobRepository;
    JobProducerService jobProducerService;
    MetabusSecurityProperties metabusSecurityProperties;
    JobMapper jobMapper;

    JobServiceImpl jobService;
    ScheduledBatchesRepository scheduledBatchesRepository;


    @BeforeEach
    public void setUp() {
        jobRepository = mock(JobRepository.class);
        jobMapper = mock(JobMapper.class);
        scheduledBatchesRepository = mock(ScheduledBatchesRepository.class);
        metabusSecurityProperties = new MetabusSecurityProperties();
        
        JobTypeAuthorization jobTypeAuthorization = new JobTypeAuthorization();
        jobTypeAuthorization.setAllowedJobTypes(Arrays.asList("bolnisi_lot", "bolnisi_cert"));
        jobTypeAuthorization.setRoles(Arrays.asList("BOLNISI_APPLICATION", "ADMIN"));
        metabusSecurityProperties.setJobTypeAuthorizations(Arrays.asList(jobTypeAuthorization));
        jobProducerService = mock(JobProducerService.class);
        jobService = new JobServiceImpl(jobRepository, jobProducerService, metabusSecurityProperties,
                jobMapper, scheduledBatchesRepository);
    }

    private void mockSecurityRoleUser() {
        Jwt jwt = mock(Jwt.class);
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("BOLNISI_APPLICATION"));
        claims.put("realm_access", realmAccess);

        when(jwt.getClaims()).thenReturn(claims);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(jwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void test_create_job_success() {
        // Prepare data
        MetabusSecurityProperties.JobTypeAuthorization jobTypeAuthorization = new MetabusSecurityProperties.JobTypeAuthorization();
        jobTypeAuthorization.setRoles(Arrays.asList("BOLNISI_APPLICATION"));
        jobTypeAuthorization.setAllowedJobTypes(Arrays.asList("bolnisi_lot", "bolnisi_cert"));

        // Create a mock Job object
        BusinessData businessData = BusinessData.builder()
                .type("bolnisi_lot")
                .build();
        Job job = Job.builder()
                .businessData(businessData)
                .build();
        job.setBusinessData(businessData);

        JobJPA jobJPA = new JobJPA();
        JobJPA savedJobJPA = new JobJPA();
        Job createdJob = Job.builder().build();

        final ScheduledBatchesJPA scheduledBatchesJPA = ScheduledBatchesJPA.builder().jobType("bolnisi_lot")
                .batchStatus(BatchStatus.PENDING).consumedJobTime(Instant.now()).build();
        final List<ScheduledBatchesJPA> scheduledBatchesJPAs = List.of(scheduledBatchesJPA);

        //mock
        mockSecurityRoleUser();
        when(jobMapper.toJobJpa(job)).thenReturn(jobJPA);
        when(jobRepository.save(jobJPA)).thenReturn(savedJobJPA);
        when(jobMapper.toJob(savedJobJPA)).thenReturn(createdJob);


        // Mock the scheduled batch return not empty
        doReturn(scheduledBatchesJPAs).when(scheduledBatchesRepository).findByJobType("bolnisi_lot");
        doReturn(scheduledBatchesJPAs).when(scheduledBatchesRepository).saveAll(scheduledBatchesJPAs);

        // Create a mock response
        BaseResponse<String> mockResponse = new BaseResponse<>();

        // Mock the job producer service response
        when(jobProducerService.createJob(job)).thenReturn(mockResponse);

        Job actualJob = jobService.createJob(job);

        assertEquals(job.getId(), actualJob.getId());
        assertEquals(job.getState(), actualJob.getState());
        assertEquals(job.getGroup(), actualJob.getGroup());
        assertEquals(job.getGroupType(), actualJob.getGroupType());
        verify(scheduledBatchesRepository, never()).save(any());
    }

    @Test
    void test_create_job_success_with_non_existed_schedule() {
        // Prepare data
        MetabusSecurityProperties.JobTypeAuthorization jobTypeAuthorization = new MetabusSecurityProperties.JobTypeAuthorization();
        jobTypeAuthorization.setRoles(Arrays.asList("BOLNISI_APPLICATION"));
        jobTypeAuthorization.setAllowedJobTypes(Arrays.asList("bolnisi_lot", "bolnisi_cert"));

        // Create a mock Job object
        BusinessData businessData = BusinessData.builder()
                .type("bolnisi_lot")
                .build();
        Job job = Job.builder()
                .businessData(businessData)
                .build();
        job.setBusinessData(businessData);

        JobJPA jobJPA = new JobJPA();
        JobJPA savedJobJPA = new JobJPA();
        Job createdJob = Job.builder().build();

        final List<ScheduledBatchesJPA> scheduledBatchesJPAs = List.of();
        final ScheduledBatchesJPA scheduledBatchesJPA = ScheduledBatchesJPA.builder().jobType("bolnisi_lot")
                .batchStatus(BatchStatus.PENDING).consumedJobTime(Instant.now()).build();

        // mock
        mockSecurityRoleUser();
        when(jobMapper.toJobJpa(job)).thenReturn(jobJPA);
        when(jobRepository.save(jobJPA)).thenReturn(savedJobJPA);
        when(jobMapper.toJob(savedJobJPA)).thenReturn(createdJob);

        // Mock the scheduled batch return not empty
        doReturn(scheduledBatchesJPAs).when(scheduledBatchesRepository).findByJobType("bolnisi_lot");
        doReturn(scheduledBatchesJPA).when(scheduledBatchesRepository).save(any());

        // Create a mock response
        BaseResponse<String> mockResponse = new BaseResponse<>();

        // Mock the job producer service response
        when(jobProducerService.createJob(job)).thenReturn(mockResponse);

        Job actualJob = jobService.createJob(job);

        assertEquals(job.getId(), actualJob.getId());
        assertEquals(job.getState(), actualJob.getState());
        assertEquals(job.getGroup(), actualJob.getGroup());
        assertEquals(job.getGroupType(), actualJob.getGroupType());
        verify(scheduledBatchesRepository, never()).saveAll(any());
    }

    private void mockSecurityRoleUserOpenBadges() {
        Jwt jwt = mock(Jwt.class);
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("OTHER_ROLE"));
        claims.put("realm_access", realmAccess);

        when(jwt.getClaims()).thenReturn(claims);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn(jwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void test_create_job_throw_metabus_exception() {
        // Create a mock Job object
        BusinessData businessData = BusinessData.builder()
                .type("bolnisi_lot")
                .build();
        Job job = Job.builder()
                .businessData(businessData)
                .build();
        job.setBusinessData(businessData);

        //mock
        mockSecurityRoleUserOpenBadges();
        MetabusException exception = assertThrows(MetabusException.class, () -> jobService.createJob(job));
        assertEquals(MetabusErrors.FORBIDDEN, exception.getError());
    }

    @Test
    void test_create_job_throw_exception() {
        // Create a mock Job object
        BusinessData businessData = BusinessData.builder()
                .type("bolnisi_lot")
                .build();
        Job job = Job.builder()
                .businessData(businessData)
                .build();
        job.setBusinessData(businessData);
        mockSecurityRoleUser();

        when(jobMapper.toJobJpa(job)).thenThrow(new RuntimeException("Error saving job"));

        MetabusException exception = assertThrows(MetabusException.class, () -> jobService.createJob(job));
        assertEquals(MetabusErrors.ERROR_CREATING_JOB, exception.getError());
    }

    @Test
    void test_get_job() {
        // Arrange
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        // Act and Assert
        MetabusException exception = assertThrows(MetabusException.class, () -> jobService.getJob(1L));
        assertEquals(MetabusErrors.ERROR_GETTING_JOB, exception.getError());
    }

    @Test
    void testGetJobSuccess() {
        // Arrange
        JobJPA jobJPA = new JobJPA();
        jobJPA.setId(1L);
        jobJPA.setType("bolnisi_lot");
        when(jobRepository.findById(1L)).thenReturn(Optional.of(jobJPA));
        when(jobMapper.toJob(jobJPA)).thenReturn(Job.builder().build());
        mockSecurityRoleUser();

        // Act
        Job result = jobService.getJob(1L);

        // Assert
        verify(jobRepository, times(1)).findById(1L);
        verify(jobMapper, times(1)).toJob(jobJPA);
        assertNotNull(result);
    }

}
