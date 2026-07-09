package com.techtalk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理后台仪表盘数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    /** 用户总数 */
    private Long totalUsers;
    /** 今日新增用户 */
    private Long todayNewUsers;
    /** 帖子总数 */
    private Long totalPosts;
    /** 今日新增帖子 */
    private Long todayNewPosts;
    /** 评论总数 */
    private Long totalComments;
    /** 今日新增评论 */
    private Long todayNewComments;
    /** 今日浏览量 */
    private Long todayViews;
    /** 在线用户数（从 Redis 获取） */
    private Long onlineUsers;

    /** 近7天每日发帖趋势 */
    private Object weeklyPostTrend;
}
