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
    `resin` int(11) NOT NULL COMMENT '树脂数',
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

CREATE TABLE `kotoumi_genshin_wish_mode` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `wish_mode` int(11) NOT NULL COMMENT '招募模式，0普通，1快速，2无图',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人祈愿池招募模式';

CREATE TABLE `kotoumi_genshin_saint_suit` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `suit_name` varchar(20) NOT NULL COMMENT '套装简称',
    `pos` int(11) NOT NULL COMMENT '位置，0-4',
    `saint_name` varchar(20) NOT NULL COMMENT '列表形式，圣遗物套装中各圣遗物名称',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人圣遗物祈愿套装';

CREATE TABLE `kotoumi_genshin_saint_wish` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `user_id` bigint(20) NOT NULL COMMENT '用户id',
    `saint_name` varchar(20) NOT NULL COMMENT '圣遗物名称',
    `pos` int(11) NOT NULL COMMENT '位置，0-4',
    `level` int(11) NOT NULL COMMENT '圣遗物等级',
    `score` varchar(10) NOT NULL COMMENT '圣遗物分数',
    `ratio` varchar(10) NOT NULL COMMENT '圣遗物分位',
    `main_property` varchar(256) NOT NULL COMMENT '主属性',
    `sub_properties` varchar(1024) NOT NULL COMMENT '副属性',
    `wish_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `enable` tinyint NOT NULL DEFAULT 1 COMMENT '是否有效',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人圣遗物祈愿结果';

CREATE TABLE `kotoumi_genshin_manage_user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增id',
    `username` varchar(64) NOT NULL COMMENT '用户名',
    `password` varchar(64) NOT NULL COMMENT '密码',
    `qq_number` varchar(20) NOT NULL COMMENT 'qq号',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi管理员表';

