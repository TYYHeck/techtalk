package com.techtalk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.techtalk.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT DISTINCT conversation_id FROM tt_chat_message " +
            "WHERE sender_id = #{userId} OR receiver_id = #{userId} ORDER BY " +
            "(SELECT MAX(created_at) FROM tt_chat_message m2 WHERE m2.conversation_id = tt_chat_message.conversation_id) DESC")
    List<String> findConversationsByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM tt_chat_message WHERE conversation_id = #{conversationId} " +
            "ORDER BY created_at ASC")
    List<ChatMessage> findByConversationId(@Param("conversationId") String conversationId);

    @Update("UPDATE tt_chat_message SET is_read = true " +
            "WHERE conversation_id = #{conversationId} AND receiver_id = #{userId} AND is_read = false")
    int markAsRead(@Param("conversationId") String conversationId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM tt_chat_message WHERE receiver_id = #{userId} AND is_read = false")
    int countUnread(@Param("userId") Long userId);

    @Select("SELECT * FROM tt_chat_message WHERE conversation_id = #{conversationId} " +
            "ORDER BY created_at DESC LIMIT 1")
    ChatMessage findLastMessage(@Param("conversationId") String conversationId);
}
