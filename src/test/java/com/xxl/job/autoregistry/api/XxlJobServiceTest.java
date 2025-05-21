package com.xxl.job.autoregistry.api;

import com.xxl.job.autoregistry.api.dto.XxlJobGroupDTO;
import com.xxl.job.autoregistry.api.dto.XxlJobInfoDTO;
import com.xxl.job.autoregistry.api.vo.XxlJobGroupVO;
import com.xxl.job.autoregistry.api.vo.XxlJobInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Slf4j
@SpringBootTest
class XxlJobServiceTest {

    @Autowired
    private XxlJobService xxlJobService;

    @Test
    void queryAllGroup() {
        List<XxlJobGroupVO> xxlJobGroupDTOS = xxlJobService.queryAllGroup();
        for (XxlJobGroupVO xxlJobGroupDTO : xxlJobGroupDTOS) {
            System.out.println(xxlJobGroupDTO);
        }
    }

    @Test
    void saveGroup() {
        String appname = UUID.randomUUID().toString();
        XxlJobGroupDTO xxlJobGroupDTO = new XxlJobGroupDTO();
        xxlJobGroupDTO.setAppname(appname);
        xxlJobGroupDTO.setTitle(appname);
        xxlJobService.saveGroup(xxlJobGroupDTO);
        List<XxlJobGroupVO> xxlJobGroupDTOS = xxlJobService.queryAllGroup();
        Integer groupId = xxlJobGroupDTOS.stream().filter(item -> item.getAppname().equals(appname)).map(XxlJobGroupVO::getId).findFirst().orElse(null);
        assert groupId != null;
        xxlJobService.removeGroup(groupId);
    }

    @Test
    void findGroupOrSaveIfNotExists() {
        String appname = UUID.randomUUID().toString();
        XxlJobGroupDTO xxlJobGroupDTO = new XxlJobGroupDTO();
        xxlJobGroupDTO.setAppname(appname);
        xxlJobGroupDTO.setTitle(appname);
        XxlJobGroupVO groupOrSaveIfNotExists = xxlJobService.findGroupOrSaveIfNotExists(xxlJobGroupDTO);
        assert groupOrSaveIfNotExists != null;
        List<XxlJobGroupVO> xxlJobGroupDTOS = xxlJobService.queryAllGroup();
        Integer groupId = xxlJobGroupDTOS.stream().filter(item -> item.getAppname().equals(appname)).map(XxlJobGroupVO::getId).findFirst().orElse(null);
        assert groupId != null;
        xxlJobService.removeGroup(groupId);
    }


    @Test
    void queryAllJobs() {
        List<XxlJobGroupVO> xxlJobGroupDTOS = xxlJobService.queryAllGroup();
        for (XxlJobGroupVO group : xxlJobGroupDTOS) {
            List<XxlJobInfoVO> jobs = xxlJobService.queryAllJobs(group.getId());
            assert jobs != null;
        }
    }


    @Test
    void addJobOrUpdate() {
        String handler = UUID.randomUUID().toString();
        List<XxlJobGroupVO> xxlJobGroupDTOS = xxlJobService.queryAllGroup();
        for (XxlJobGroupVO group : xxlJobGroupDTOS) {
            XxlJobInfoDTO jobParam = new XxlJobInfoDTO(group.getId(), "test", "lianweimao", "0 0 * * * ?", handler);
            Map<Boolean, List<XxlJobInfoDTO>> resultMap = xxlJobService.addJobOrUpdate(List.of(jobParam));
            assert resultMap.get(true).size() > 0;
            xxlJobService.stopAndRemoveJob(resultMap.get(true).get(0).getId());
        }
    }
}