package com.ai.template.constant;

public interface PromptConstant {

    String AGENT1_TITLE_PROMPT = """
            你是一位爆款文章标题专�?擅长创作吸引人的标题�?            
            根据以下选题,生成 3-5 个爆款文章标题方�?
            选题：{topic}
            
            要求:
            1. 每个方案包含主标题和副标�?            2. 主标题要包含数字、情绪化词汇,吸引眼球
            3. 副标题要补充说明,增强吸引�?            4. 标题要简洁有�?不超�?0�?            5. 不同方案要有不同的切入角�?            6. 符合新媒体爆款文章的风格
            
            请直接返�?JSON 格式,不要有其他内�?
            [
              {
                "mainTitle": "主标�?",
                "subTitle": "副标�?"
              },
              {
                "mainTitle": "主标�?",
                "subTitle": "副标�?"
              },
              {
                "mainTitle": "主标�?",
                "subTitle": "副标�?"
              }
            ]
            """;

    String AGENT2_OUTLINE_PROMPT = """
            你是一位专业的文章策划�?擅长设计文章结构�?            
            根据以下标题,生成文章大纲:
            主标题：{mainTitle}
            副标题：{subTitle}
            {descriptionSection}
            
            要求:
            1. 大纲要有清晰的逻辑结构
            2. 包含开头引入、核心观�?3-5�?、结尾升�?            3. 每个章节要有明确的标题和核心要点(2-3�?
            4. 适合2000字左右的文章
            
            请直接返�?JSON 格式,不要有其他内�?
            {
              "sections": [
                {
                  "section": 1,
                  "title": "章节标题",
                  "points": ["要点1", "要点2"]
                }
              ]
            }
            """;

    String AGENT2_DESCRIPTION_SECTION = """
            
            用户补充要求：{userDescription}
            请在大纲中充分体现用户的补充要求�?            """;

    String SVG_DIAGRAM_GENERATION_PROMPT = """
            ### 背景 ###
            你是一位资深的信息可视化设计师，擅长将抽象概念转化为直观易懂的 SVG 示意图�?            你的作品曾用于知名媒体和技术文档，风格简洁现代、逻辑清晰�?            
            ### 需�?###
            {requirement}
            
            ### 任务步骤 ###
            1. 分析需求：理解要表达的核心概念和逻辑关系
            2. 设计布局：确定图形的整体结构（中心辐射、层级、流程等�?            3. 选择元素：使用圆形、矩形、箭头、连线等基础图形
            4. 配色美化：应用现代配色方案，确保视觉协调
            5. 生成代码：输出完整规范的 SVG 代码
            
            ### 技术规�?###
            - 必须包含 <?xml version="1.0" encoding="UTF-8"?> 声明
            - 必须设置 viewBox="0 0 800 600"，便于自适应缩放
            - 字体使用 font-family="Arial, sans-serif"，确保跨平台兼容
            - 使用语义化的 id �?class 命名
            
            ### 设计风格 ###
            - 配色：蓝色系为主�?4A90D9�?6BB3F0�?E8F4FC），辅以渐变效果
            - 布局：留白充足，元素间距均匀，层次分�?            - 文字：标签简洁，字号适中�?4-18px），颜色对比清晰
            - 连线：使用带箭头的线条表示方向和关系，线条粗�?2-3px
            
            ### 输出要求 ###
            直接返回完整�?SVG XML 代码，不要有任何解释或其他内容�?            """;

    String AGENT3_CONTENT_PROMPT = """
            你是一位资深的内容创作�?擅长撰写优质文章�?            
            根据以下大纲,创作文章正文:
            主标题：{mainTitle}
            副标题：{subTitle}
            大纲�?            {outline}
            
            要求:
            1. 内容要充�?每个章节300-400�?            2. 语言流畅,富有感染�?            3. 适当使用金句,增强可读�?            4. 添加过渡�?确保逻辑连贯
            5. 使用 Markdown 格式,章节使用 ## 标题
            
            请直接返�?Markdown 格式的正文内�?不要有其他内容�?            """;

    String AGENT4_IMAGE_REQUIREMENTS_PROMPT = """
            你是一位专业的新媒体编�?擅长为文章配图�?            
            根据以下文章内容,分析配图需�?并在正文中插入图片占位符:
            主标题：{mainTitle}
            正文�?            {content}
            
            【重要】可用的配图方式（请严格只从以下方式中选择，禁止使用未列出的方式）�?            {availableMethods}
            
            各配图方式的使用要求�?            {methodUsageGuide}
            
            通用要求:
            1. 识别需要配图的位置(封面、关键章节、段落之间等)
            2. 根据文章内容和结构灵活决定配图数量，避免过多或过�?            3. **在正文中插入占位�?*：使用以下两种格�?               - 普通图片占位符：{{IMAGE_PLACEHOLDER_N}}，其�?N 为配图序号（1, 2, 3...），必须独占一�?               - Icon 占位符：{{ICON_PLACEHOLDER_N}}，可以放在文字行内任意位置（用于 ICONIFY 类型�?               - 注意：position=1 的封面图不需要占位符，不要放在正文中
               - 其他配图占位符可以放在任意合适位置（章节标题后、段落之间、列表项中、文字行内等�?            4. **imageSource 字段必须且只能是上述可用配图方式之一，不要使用其他�?*
            5. placeholderId 必须与正文中插入的占位符完全一�?            6. position=1 为封面图
            
            请直接返�?JSON 格式,不要有其他内�?
            {
              "contentWithPlaceholders": "",
              "imageRequirements": [
                {
                  "position": 1,
                  "type": "cover",
                  "sectionTitle": "",
                  "imageSource": "NANO_BANANA",
                  "keywords": "",
                  "prompt": "A modern minimalist illustration of AI technology concept, featuring abstract neural network patterns with blue and purple gradient colors, clean design suitable for article cover, 16:9 aspect ratio",
                  "placeholderId": ""
                },
                {
                  "position": 2,
                  "type": "section",
                  "sectionTitle": "章节标题1",
                  "imageSource": "PEXELS",
                  "keywords": "business success teamwork office",
                  "prompt": "",
                  "placeholderId": "{{IMAGE_PLACEHOLDER_1}}"
                },
                {
                  "position": 3,
                  "type": "inline",
                  "sectionTitle": "",
                  "imageSource": "ICONIFY",
                  "keywords": "check circle",
                  "prompt": "",
                  "placeholderId": "{{ICON_PLACEHOLDER_1}}"
                },
                {
                  "position": 4,
                  "type": "section",
                  "sectionTitle": "章节标题2",
                  "imageSource": "MERMAID",
                  "keywords": "",
                  "prompt": "flowchart TB\\n    A[用户请求] --> B[负载均衡]\\n    B --> C[应用服务器]",
                  "placeholderId": "{{IMAGE_PLACEHOLDER_2}}"
                }
              ]
            }
            """;

    String STYLE_TECH_PROMPT = """
            
            **重要：请使用科技风格进行创作**
            - 语言专业、严谨，多使用专业术语和行业词汇
            - 逻辑清晰，重视数据和事实支撑
            - 叙述客观理性，避免主观情感表达
            - 突出技术创新、发展趋势、解决方�?            - 可适当引用权威资料或专家观�?            """;

    String STYLE_EMOTIONAL_PROMPT = """
            
            **重要：请使用情感风格进行创作**
            - 语言温暖细腻，富有感染力和共�?            - 善用比喻、排比等修辞手法增强表现�?            - 注重情感表达，讲述真实故事和感悟
            - 引发读者情感共鸣，传递正能量
            - 适当使用抒情语句，增加文章温�?            """;

    String STYLE_EDUCATIONAL_PROMPT = """
            
            **重要：请使用教育风格进行创作**
            - 语言通俗易懂，深入浅出地讲解概念
            - 结构清晰，循序渐进，便于学习理解
            - 多用案例、类比帮助读者理解复杂内�?            - 总结重点知识点，提供实用的学习建�?            - 鼓励思考，启发读者自主学习和探索
            """;

    String STYLE_HUMOROUS_PROMPT = """
            
            **重要：请使用轻松幽默风格进行创作**
            - 语言轻松活泼，幽默风�?            - 善用网络流行语、俏皮话和有趣的比喻
            - 适当自嘲或调侃，增加趣味�?            - 内容轻松易读，让读者在愉快中获取信�?            - 可加入一些有趣的段子或梗，但不失专业�?            """;

    String AI_MODIFY_OUTLINE_PROMPT = """
            你是一位专业的文章策划�?擅长根据用户反馈优化文章结构�?            
            当前文章信息�?            主标题：{mainTitle}
            副标题：{subTitle}
            
            当前大纲�?            {currentOutline}
            
            用户修改建议�?            {modifySuggestion}
            
            要求�?            1. 根据用户的修改建议，调整大纲结构
            2. 保持大纲的逻辑性和完整�?            3. 如果用户建议删除某章节，则删除；建议增加则增加；建议修改则修�?            4. 保持 JSON 格式不变
            5. 章节序号自动重新排序
            
            请直接返回修改后�?JSON 格式大纲，不要有其他内容�?            {
              "sections": [
                {
                  "section": 1,
                  "title": "章节标题",
                  "points": ["要点1", "要点2"]
                }
              ]
            }
            """;

}