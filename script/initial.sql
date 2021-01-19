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
    `wish_event_id` int(11) NOT NULL COMMENT '祈愿池子id，1（角色）/2（武器）/3（混合）',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='kotoumi机器人祈愿池';

insert into kotoumi_genshin_unit values (1001, 1, '安柏', 4);
insert into kotoumi_genshin_unit values (1002, 1, '凯亚', 4);
insert into kotoumi_genshin_unit values (1003, 1, '丽莎', 4);
insert into kotoumi_genshin_unit values (1004, 1, '琴', 5);
insert into kotoumi_genshin_unit values (1005, 1, '可莉', 5);
insert into kotoumi_genshin_unit values (1006, 1, '诺艾尔', 4);
insert into kotoumi_genshin_unit values (1007, 1, '芭芭拉', 4);
insert into kotoumi_genshin_unit values (1008, 1, '温迪', 5);
insert into kotoumi_genshin_unit values (1009, 1, '雷泽', 4);
insert into kotoumi_genshin_unit values (1010, 1, '迪卢克', 5);
insert into kotoumi_genshin_unit values (1011, 1, '班尼特', 4);
insert into kotoumi_genshin_unit values (1012, 1, '菲谢尔', 4);
insert into kotoumi_genshin_unit values (1013, 1, '北斗', 4);
insert into kotoumi_genshin_unit values (1014, 1, '凝光', 4);
insert into kotoumi_genshin_unit values (1015, 1, '香菱', 4);
insert into kotoumi_genshin_unit values (1016, 1, '行秋', 4);
insert into kotoumi_genshin_unit values (1017, 1, '重云', 4);
insert into kotoumi_genshin_unit values (1018, 1, '砂糖', 4);
insert into kotoumi_genshin_unit values (1019, 1, '莫娜', 5);
insert into kotoumi_genshin_unit values (1020, 1, '刻晴', 5);
insert into kotoumi_genshin_unit values (1021, 1, '七七', 5);
insert into kotoumi_genshin_unit values (1022, 1, '达达利亚', 5);
insert into kotoumi_genshin_unit values (1023, 1, '迪奥娜', 4);
insert into kotoumi_genshin_unit values (1024, 1, '钟离', 5);
insert into kotoumi_genshin_unit values (1025, 1, '辛焱', 4);
insert into kotoumi_genshin_unit values (1026, 1, '阿贝多', 5);
insert into kotoumi_genshin_unit values (1027, 1, '甘雨', 5);

insert into kotoumi_genshin_unit values (2001, 2, '飞天御剑', 3);
insert into kotoumi_genshin_unit values (2002, 2, '黎明神剑', 3);
insert into kotoumi_genshin_unit values (2003, 2, '冷刃', 3);
insert into kotoumi_genshin_unit values (2004, 2, '笛剑', 4);
insert into kotoumi_genshin_unit values (2005, 2, '祭礼剑', 4);
insert into kotoumi_genshin_unit values (2006, 2, '黑岩长剑', 4);
insert into kotoumi_genshin_unit values (2007, 2, '西风剑', 4);
insert into kotoumi_genshin_unit values (2008, 2, '匣里龙吟', 4);
insert into kotoumi_genshin_unit values (2009, 2, '风鹰剑', 5);
insert into kotoumi_genshin_unit values (2010, 2, '天空之刃', 5);
insert into kotoumi_genshin_unit values (2011, 2, '斫峰之刃', 5);
insert into kotoumi_genshin_unit values (2012, 2, '沐浴龙血的剑', 3);
insert into kotoumi_genshin_unit values (2013, 2, '以理服人', 3);
insert into kotoumi_genshin_unit values (2014, 2, '铁影阔剑', 3);
insert into kotoumi_genshin_unit values (2015, 2, '祭礼大剑', 4);
insert into kotoumi_genshin_unit values (2016, 2, '雨裁', 4);
insert into kotoumi_genshin_unit values (2017, 2, '钟剑', 4);
insert into kotoumi_genshin_unit values (2018, 2, '西风大剑', 4);
insert into kotoumi_genshin_unit values (2019, 2, '黑岩斩刀', 4);
insert into kotoumi_genshin_unit values (2020, 2, '狼的末路', 5);
insert into kotoumi_genshin_unit values (2021, 2, '天空之傲', 5);
insert into kotoumi_genshin_unit values (2022, 2, '无工之剑', 5);
insert into kotoumi_genshin_unit values (2023, 2, '神射手之誓', 3);
insert into kotoumi_genshin_unit values (2024, 2, '鸦羽弓', 3);
insert into kotoumi_genshin_unit values (2025, 2, '弹弓', 3);
insert into kotoumi_genshin_unit values (2026, 2, '西风猎弓', 4);
insert into kotoumi_genshin_unit values (2027, 2, '弓藏', 4);
insert into kotoumi_genshin_unit values (2028, 2, '祭礼弓', 4);
insert into kotoumi_genshin_unit values (2029, 2, '绝弦', 4);
insert into kotoumi_genshin_unit values (2030, 2, '黑岩战弓', 4);
insert into kotoumi_genshin_unit values (2031, 2, '阿莫斯之弓', 5);
insert into kotoumi_genshin_unit values (2032, 2, '天空之翼', 5);
insert into kotoumi_genshin_unit values (2033, 2, '黑缨枪', 3);
insert into kotoumi_genshin_unit values (2034, 2, '匣里灭辰', 4);
insert into kotoumi_genshin_unit values (2035, 2, '黑岩刺枪', 4);
insert into kotoumi_genshin_unit values (2036, 2, '西风长枪', 4);
insert into kotoumi_genshin_unit values (2037, 2, '天空之脊', 5);
insert into kotoumi_genshin_unit values (2038, 2, '和璞鸢', 5);
insert into kotoumi_genshin_unit values (2039, 2, '贯虹之槊', 5);
insert into kotoumi_genshin_unit values (2040, 2, '魔导绪论', 3);
insert into kotoumi_genshin_unit values (2041, 2, '讨龙英杰谭', 3);
insert into kotoumi_genshin_unit values (2042, 2, '翡玉法球', 3);
insert into kotoumi_genshin_unit values (2043, 2, '祭礼残章', 4);
insert into kotoumi_genshin_unit values (2044, 2, '流浪乐章', 4);
insert into kotoumi_genshin_unit values (2045, 2, '西风秘典', 4);
insert into kotoumi_genshin_unit values (2046, 2, '宗室秘法录', 4);
insert into kotoumi_genshin_unit values (2047, 2, '黑岩绯玉', 4);
insert into kotoumi_genshin_unit values (2048, 2, '昭心', 4);
insert into kotoumi_genshin_unit values (2049, 2, '四风原典', 5);
insert into kotoumi_genshin_unit values (2050, 2, '天空之卷', 5);
insert into kotoumi_genshin_unit values (2051, 2, '讨尘世之锁', 5);
