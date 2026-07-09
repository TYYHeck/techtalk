package com.techtalk.controller.admin;

import com.techtalk.common.Result;
import com.techtalk.mapper.CommentMapper;
import com.techtalk.mapper.PostMapper;
import com.techtalk.mapper.UserMapper;
import com.techtalk.util.RedisUtil;
import com.techtalk.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端 - 仪表盘
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final RedisUtil redisUtil;

    @GetMapping
    public Result<DashboardVO> getDashboard() {
        // 统计总数
        Long totalUsers = userMapper.selectCount(null);
        Long totalPosts = postMapper.countPosts();
        Long totalComments = commentMapper.countComments();
        Long todayNewPosts = postMapper.countTodayPosts();
        Long todayNewComments = commentMapper.countTodayComments();

        // 今日新增用户
        Long todayNewUsers = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.techtalk.entity.User>()
                        .apply("DATE(created_at) = CURDATE()"));

        // 在线用户（通过 Redis keys 计数）  
        Long onlineUsers = redisUtil.countKeys("online:*");

        // 近7天发帖趋势
        var weeklyTrend = postMapper.selectWeeklyPostTrend();

        DashboardVO dashboard = DashboardVO.builder()
                .totalUsers(totalUsers)
                .todayNewUsers(todayNewUsers)
                .totalPosts(totalPosts)
                .todayNewPosts(todayNewPosts)
                .totalComments(totalComments)
                .todayNewComments(todayNewComments)
                .todayViews(0L) // 可扩展
                .onlineUsers(onlineUsers)
                .weeklyPostTrend(weeklyTrend)
                .build();

        return Result.ok(dashboard);
    }
}
