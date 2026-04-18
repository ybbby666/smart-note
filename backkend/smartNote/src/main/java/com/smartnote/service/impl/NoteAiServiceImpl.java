package com.smartnote.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartnote.dto.NoteAiResultDTO;
import com.smartnote.exception.BusinessException;
import com.smartnote.mapper.NoteAiAnalysisMapper;
import com.smartnote.mapper.NoteMapper;
import com.smartnote.pojo.Note;
import com.smartnote.pojo.NoteAiAnalysis;
import com.smartnote.service.NoteAiService;
import com.smartnote.vo.NoteAiResultVO;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NoteAiServiceImpl extends ServiceImpl<NoteAiAnalysisMapper, NoteAiAnalysis> implements NoteAiService {
    @Autowired
    private NoteAiAnalysisMapper noteAiAnalysisMapper;
    @Autowired
    private NoteMapper noteMapper;
    
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;
    
    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
    private NoteAiResultVO buildVO(Long noteId, NoteAiResultDTO result) {
        com.smartnote.vo.NoteAiResultVO vo = new NoteAiResultVO();
        vo.setNoteId(noteId);
        vo.setSummary(result.getSummary());
        vo.setKeyPoints(result.getKeyPoints());
        vo.setTags(result.getTags());
        return vo;
    }
    private String extractJson(String text) {
        int startIndex = text.indexOf("{");
        int endIndex = text.lastIndexOf("}");
        if (startIndex >= 0 && endIndex > startIndex) {
            return text.substring(startIndex, endIndex + 1);
        }
        return text;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoteAiResultVO analyzeNote(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该笔记");
        }
        String promptText = """
                你是一个专业的笔记分析助手。
                请对以下笔记内容进行智能分析，严格按照以下 JSON 格式返回，**不要包含任何多余的解释、文字或代码**，只返回 JSON 字符串本身。
                
                {
                  "summary": "笔记摘要",
                  "keyPoints": ["关键点1", "关键点2"],
                  "tags": ["标签1", "标签2"]
                }
                
                笔记内容：
                """ + note.getContent();
        log.info("开始调用AI分析接口,笔记ID:{}", noteId);
        String aiResponse;
        try {
            //创建服务端
            ArkService arkService = ArkService.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .build();
            // 构建消息
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER)
                    .content(promptText)
                    .build());
            // 构建请求
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .build();
            Object content = arkService.createChatCompletion(request).getChoices().get(0).getMessage().getContent();
            aiResponse = content != null ? content.toString() : "";//防止空指针异常
            log.info("AI接口调用成功,响应长度:{}", aiResponse.length());
        } catch (Exception e) {
            log.error("AI接口调用异常,笔记ID:{},错误类型:{},错误信息:{}", noteId, e.getClass().getName(), e.getMessage(), e);
            throw new BusinessException("AI分析服务异常，请稍后重试");
        }
        log.info("AI原始返回: {}", aiResponse);
        NoteAiResultDTO aiResult;
        try {
            String jsonStr = extractJson(aiResponse);//提取json字符串
            aiResult = JSONUtil.toBean(jsonStr, NoteAiResultDTO.class);//转为java对象
        } catch (Exception e) {
            log.error("AI结果解析失败,原始返回: {}", aiResponse, e);
            throw new BusinessException("AI分析结果解析失败,请重试");
        }
        LambdaQueryWrapper<NoteAiAnalysis> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoteAiAnalysis::getNoteId, noteId)
                .eq(NoteAiAnalysis::getUserId, userId);
        
        // 先删除所有旧记录，避免重复
        long count = this.count(queryWrapper);
        if (count > 1) {
            log.warn("发现{}条重复的AI分析记录，清理旧数据: noteId={}, userId={}", count, noteId, userId);
            this.remove(queryWrapper);
        }
        
        NoteAiAnalysis existAnalysis = this.getOne(queryWrapper);
        if (existAnalysis != null) {
            existAnalysis.setSummary(aiResult.getSummary());
            existAnalysis.setKeyPoints(JSONUtil.toJsonStr(aiResult.getKeyPoints()));
            existAnalysis.setTags(JSONUtil.toJsonStr(aiResult.getTags()));
            this.updateById(existAnalysis);
        } else {
            NoteAiAnalysis newAnalysis = new NoteAiAnalysis();
            newAnalysis.setNoteId(noteId);
            newAnalysis.setUserId(userId);
            newAnalysis.setSummary(aiResult.getSummary());
            newAnalysis.setKeyPoints(JSONUtil.toJsonStr(aiResult.getKeyPoints()));
            newAnalysis.setTags(JSONUtil.toJsonStr(aiResult.getTags()));
            this.save(newAnalysis);
        }
        return buildVO(noteId, aiResult);
    }
    @Override
    public NoteAiResultVO getAnalysisResult(Long noteId, Long userId) {
        LambdaQueryWrapper<NoteAiAnalysis> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteAiAnalysis::getNoteId, noteId)
                .eq(NoteAiAnalysis::getUserId, userId);
        
        // 使用 list() 避免 TooManyResultsException
        List<NoteAiAnalysis> analysisList = this.list(wrapper);
        if (analysisList == null || analysisList.isEmpty()) {
            return null;
        }
        
        // 如果有多条记录，取最新的一条（按ID降序）
        NoteAiAnalysis analysis = analysisList.stream()
                .sorted((a1, a2) -> a2.getId().compareTo(a1.getId()))
                .findFirst()
                .orElse(null);
        
        if (analysis == null) {
            return null;
        }
        
        NoteAiResultVO vo = new NoteAiResultVO();
        vo.setNoteId(analysis.getNoteId());
        vo.setSummary(analysis.getSummary());
        vo.setKeyPoints(JSONUtil.toList(analysis.getKeyPoints(), String.class));
        vo.setTags(JSONUtil.toList(analysis.getTags(), String.class));
        vo.setUpdateTime(analysis.getUpdateTime());
        return vo;
    }
}
