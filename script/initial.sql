CREATE TABLE `kotoumi_keyword` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `group_id` bigint(20) NOT NULL COMMENT '群聊id',
    `creator_id` bigint(20) NOT NULL COMMENT '创建者id',
    `keyword` text NOT NULL COMMENT '关键词',
    `response` text NOT NULL COMMENT '响应话术',
    `enable` tinyint NOT NULL COMMENT '是否可用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人关键词';

CREATE TABLE `kotoumi_daily` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `group_id` bigint(20) NOT NULL COMMENT '群聊id',
    `user_id` bigint(20) NOT NULL COMMENT '用户id',
    `sign_in_result` int(11) NOT NULL COMMENT '签到结果',
    `draw_result` int(11) NOT NULL COMMENT '抽签结果',
    `divine_result` int(11) NOT NULL COMMENT '占卜结果',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人每日任务';

CREATE TABLE `kotoumi_tags` (
    `unit_number` int(11) NOT NULL COMMENT '社员编号',
    `tags` varchar(256) NOT NULL COMMENT '标签，使用|分割'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='卡片别名';

CREATE TABLE `kotoumi_genshin_primogems` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `user_id` bigint(20) NOT NULL COMMENT '用户id',
    `primogems` int(11) NOT NULL COMMENT '原石数',
    `starlight` int(11) NOT NULL COMMENT '星辉数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人原石数';

CREATE TABLE `kotoumi_genshin_wish` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `user_id` bigint(20) NOT NULL COMMENT '用户id',
    `wish_event_id` int(11) NOT NULL COMMENT '祈愿池子id',
    `unit_id` int(11) NOT NULL COMMENT '祈愿结果id',
    `wish_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人祈愿';

CREATE TABLE `kotoumi_genshin_unit` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '对象id',
    `unit_type` int(11) NOT NULL COMMENT '祈愿类型，1（角色）/2（武器）',
    `unit_name` varchar(20) NOT NULL COMMENT '对象名字',
    `rarity` int(11) NOT NULL COMMENT '对象稀有度',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人祈愿对象';

CREATE TABLE `kotoumi_genshin_wish_event` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '祈愿池id',
    `wish_event_name` varchar(20) NOT NULL COMMENT '祈愿池名称',
    `unit_five_region` varchar(256) NOT NULL COMMENT '五星范围',
    `unit_four_region` varchar(256) NOT NULL COMMENT '四星范围',
    `wish_type` int(11) NOT NULL COMMENT '祈愿类型，1为90抽保底无up池，2为80抽保底武器up（75%）池，3为90抽保底角色up（50%）池且有大保底',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `end_time` datetime NOT NULL COMMENT '结束时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人祈愿池';

CREATE TABLE `kotoumi_genshin_wish_unit_map` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '对象id',
    `wish_event_id` int(11) NOT NULL COMMENT '祈愿池子id',
    `unit_id` int(11) NOT NULL COMMENT '祈愿对象id',
    `is_up` int(11) NOT NULL COMMENT '是否是up对象',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人祈愿池和对象映射表';
