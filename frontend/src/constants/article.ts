/**
 * 文章相关常量定义
 */


  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}


  [ArticleStatus.PENDING]: '等待�?,
  [ArticleStatus.PROCESSING]: '生成�?,
  [ArticleStatus.COMPLETED]: '已完�?,
  [ArticleStatus.FAILED]: '失败',
}


  [ArticleStatus.PENDING]: 'default',
  [ArticleStatus.PROCESSING]: 'processing',
  [ArticleStatus.COMPLETED]: 'success',
  [ArticleStatus.FAILED]: 'error',
}


export const STATUS_COLOR_MAP: Record<string, string> = {
  [ArticleStatus.PENDING]: '#6B7280',
  [ArticleStatus.PROCESSING]: '#3B82F6',
  [ArticleStatus.COMPLETED]: '#22C55E',
  [ArticleStatus.FAILED]: '#EF4444',
}


export const MAX_TOPIC_LENGTH = 500
export const DEFAULT_TOTAL_IMAGES = 5


export const STATUS_OPTIONS = [
  { value: '', label: '全部状�? },
  { value: ArticleStatus.COMPLETED, label: '已完�? },
  { value: ArticleStatus.PROCESSING, label: '生成�? },
  { value: ArticleStatus.PENDING, label: '等待�? },
  { value: ArticleStatus.FAILED, label: '失败' },
]
