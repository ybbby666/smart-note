package com.smartnote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartnote.pojo.RecycleBin;
import com.smartnote.vo.RecycleBinItemVO;

import java.util.List;

public interface RecycleBinService extends IService<RecycleBin> {
    
    /**
     * 添加到回收站
     */
    void addToRecycleBin(String resourceType, Long resourceId, Long userId);
    
    /**
     * 获取回收站列表
     */
    List<RecycleBinItemVO> getRecycleBinList(Long userId);
    
    /**
     * 恢复资源
     */
    void restoreFromRecycleBin(Long recycleBinId, Long userId);
    
    /**
     * 彻底删除（从回收站删除）
     */
    void permanentDelete(Long recycleBinId, Long userId);
    
    /**
     * 清空回收站
     */
    void clearRecycleBin(Long userId);
    
    /**
     * 滑动窗口算法：清理过期项目（在访问回收站时自动触发）
     */
    void cleanExpiredItems();
}
