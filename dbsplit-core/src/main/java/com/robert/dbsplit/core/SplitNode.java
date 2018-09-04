package com.robert.dbsplit.core;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 切分的节点，节点可能是负载均衡的，或者是读写分离的
 */
public class SplitNode {
    /**
     * 这两个属性一般都是通过配置文件注入
     */
    //主节点的数据源配置
    private JdbcTemplate masterTemplate;
    //可能会陪多个数据节点
    private List<JdbcTemplate> slaveTemplates;

    private AtomicLong iter = new AtomicLong(0);

    public SplitNode() {
    }

    public SplitNode(JdbcTemplate masterTemplate, List<JdbcTemplate> slaveTemplates) {
        this.masterTemplate = masterTemplate;
        this.slaveTemplates = slaveTemplates;
    }

    public SplitNode(JdbcTemplate masterTemplate, JdbcTemplate... slaveTemplates) {
        this.masterTemplate = masterTemplate;
        this.slaveTemplates = Arrays.asList(slaveTemplates);
    }

    public JdbcTemplate getMasterTemplate() {
        return masterTemplate;
    }

    public void setMasterTemplate(JdbcTemplate masterTemplate) {
        this.masterTemplate = masterTemplate;
    }

    public List<JdbcTemplate> getSlaveTemplates() {
        return slaveTemplates;
    }

    public void setSlaveTemplates(List<JdbcTemplate> slaveTemplates) {
        this.slaveTemplates = slaveTemplates;
    }

    public void addSalveTemplate(JdbcTemplate jdbcTemplate) {
        this.slaveTemplates.add(jdbcTemplate);
    }

    public void removeSalveTemplate(JdbcTemplate jdbcTemplate) {
        this.slaveTemplates.remove(jdbcTemplate);
    }

    /**
     * 负载均衡的从节点 加入了负载均衡
     *
     * @return
     */
    public JdbcTemplate getRoundRobinSlaveTempate() {
        long iterValue = iter.incrementAndGet();

        // Still race condition, but it doesn't matter
        if (iterValue == Long.MAX_VALUE)
            iter.set(0);
        return slaveTemplates.get((int) iterValue % slaveTemplates.size());
    }

}
