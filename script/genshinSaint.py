# coding=utf-8
import random
import sys
from heapq import *
import gc

# 主属性及出现权重
main_properties = [
    {
        "生命值": 1
    },
    {
        "攻击力": 1,
    },
    {
        "生命值百分比": 8,
        "攻击力百分比": 8,
        "防御力百分比": 8,
        "充能效率": 3,
        "元素精通": 3
    },
    {
        "生命值百分比": 17,
        "攻击力百分比": 17,
        "防御力百分比": 16,
        "元素精通": 2,
        "火元素伤害加成": 4,
        "水元素伤害加成": 4,
        "冰元素伤害加成": 4,
        "雷元素伤害加成": 4,
        "风元素伤害加成": 4,
        "岩元素伤害加成": 4,
        "物理伤害加成": 4
    },
    {
        "生命值百分比": 11,
        "攻击力百分比": 11,
        "防御力百分比": 11,
        "暴击率": 5,
        "暴击伤害": 5,
        "治疗加成": 5,
        "元素精通": 2
    }
]

# 副属性及出现权重
sub_properties = {
    "生命值": 6,
    "攻击力": 6,
    "防御力": 6,
    "生命值百分比": 4,
    "攻击力百分比": 4,
    "防御力百分比": 4,
    "元素精通": 4,
    "充能效率": 4,
    "暴击率": 3,
    "暴击伤害": 3
}

# 副属性数值比例
sub_properties_value = {
    "生命值": [209.13, 239.00, 268.88, 298.75],
    "攻击力": [13.62, 15.56, 17.51, 19.45],
    "防御力": [16.20, 18.52, 20.83, 23.15],
    "生命值百分比": [4.08, 4.66, 5.25, 5.83],
    "攻击力百分比": [4.08, 4.66, 5.25, 5.83],
    "防御力百分比": [5.10, 5.83, 6.56, 7.29],
    "元素精通": [16.32, 18.65, 20.98, 23.31],
    "充能效率": [4.53, 5.18, 5.83, 6.48],
    "暴击率": [2.72, 3.11, 3.50, 3.89],
    "暴击伤害": [5.44, 6.22, 6.99, 7.77]
}

# 各位置圣遗物名称
position_name = ['生之花', '死之羽', '时之沙', '空之杯', '理之冠']

# 结果需要带%的属性名称
ratio_properties = ['生命值百分比', '攻击力百分比', '防御力百分比', '充能效率', '暴击率', '暴击伤害', '治疗加成',
                    '火元素伤害加成', '水元素伤害加成', '冰元素伤害加成', '雷元素伤害加成', '风元素伤害加成', '岩元素伤害加成',
                    '物理伤害加成']

# 计入评分的主属性
useful_main_property = [['生命值'], ['攻击力'], ['攻击力百分比'], ['火元素伤害加成', '水元素伤害加成', '冰元素伤害加成',
                        '雷元素伤害加成', '风元素伤害加成', '岩元素伤害加成', '物理伤害加成'], ['暴击率', '暴击伤害']]


def random_choose(weights, ignore_keys=None):
    """
    加权随机选择
    :param weights: [map<string, int>] 权重字典
    :param ignore_keys: [list<string>] 忽略的键
    :return:
    """
    # 转化为列表
    if ignore_keys is None:
        ignore_keys = []
    choose_key_list = []
    choose_weight_list = []
    total_weight = 0
    for k, v in weights.items():
        if k not in ignore_keys:
            choose_key_list.append(k)
            total_weight += v
            choose_weight_list.append(total_weight)

    # 随机选择
    choose_weight = random.randint(0, total_weight - 1)
    for index in range(len(choose_weight_list)):
        if choose_weight_list[index] > choose_weight:
            return choose_key_list[index]


class Saint(object):
    """
    圣遗物类
    """
    def __init__(self):
        # [int] 圣遗物位置
        self.position = None
        # [string] 圣遗物主属性
        self.main_property = None
        # [int] 圣遗物等级
        self.level = None
        # [list<map>] 圣遗物副属性
        self.sub_properties = None

    def choose_main_property(self):
        """
        随机选择主属性
        :return: [string] 主属性
        """
        return random_choose(main_properties[self.position])

    def choose_sub_property(self):
        """
        随机选择副属性，当副属性小于4个时，生成新的副属性，否则进行强化
        """
        if len(self.sub_properties) < 4:
            # 生成新的副属性，且不合主属性与已有副属性重合
            ignore_properties = list()
            for sub_property in self.sub_properties:
                ignore_properties.append(sub_property['property'])
            ignore_properties.append(self.main_property)
            choose_property = random_choose(sub_properties, ignore_properties)
            choose_value = sub_properties_value[choose_property][random.randint(0, 3)]
            self.sub_properties.append({"property": choose_property, "value": choose_value})
        else:
            # 在已有副属性中进行强化
            current_weights = dict()
            for sub_property in self.sub_properties:
                current_weights[sub_property['property']] = 1
            choose_property = random_choose(current_weights)
            choose_value = sub_properties_value[choose_property][random.randint(0, 3)]
            for sub_property in self.sub_properties:
                if sub_property['property'] == choose_property:
                    sub_property['value'] += choose_value

    def print_saint(self):
        """
        格式化输出圣遗物
        :return:
        """
        sub_properties_str = ""
        for sub_property in self.sub_properties:
            if sub_property['property'] in ratio_properties:
                sub_properties_str += "%s\t%.1f%%\n" % (sub_property['property'].replace('百分比', ''),
                                                        sub_property['value'])
            else:
                sub_properties_str += "%s\t%d\n" % (sub_property['property'], sub_property['value'])
        print("%s [+%d] %s\n%s" % (position_name[self.position], self.level, self.main_property, sub_properties_str))

    def strength(self):
        """
        强化圣遗物
        :return:
        """
        self.level += 4
        self.choose_sub_property()

    def score(self):
        """
        圣遗物评分，元素精通和充能效率分开来评
        :return:
        """
        if self.main_property not in useful_main_property[self.position]:
            return 0, 0
        score = 0
        element_score = 0
        for sub_property in self.sub_properties:
            if sub_property['property'] == '暴击伤害':
                score += sub_property['value']
            elif sub_property['property'] == '暴击率':
                score += sub_property['value'] * 2
            elif sub_property['property'] == '攻击力百分比':
                score += sub_property['value']
            elif sub_property['property'] == '攻击力':
                score += sub_property['value'] * 0.15
        return score, element_score

    @staticmethod
    def generate_saint(pos=None):
        """
        构造一个+0圣遗物，有20%概率生成4词条
        :param pos: [int] 是否指定圣遗物位置，不指定时随机生成
        :return:
        """
        saint = Saint()
        if pos is None:
            saint.position = random.randint(0, 4)
        else:
            saint.position = pos
        saint.main_property = saint.choose_main_property()
        saint.sub_properties = list()
        saint.choose_sub_property()
        saint.choose_sub_property()
        saint.choose_sub_property()
        if random.random() < 0.2:
            saint.choose_sub_property()
        saint.level = 0
        return saint


if __name__ == '__main__':
    pos = int(sys.argv[1])
    print("Processing pos = %d" % pos)
    total_count = 100000000
    heap_count = total_count / 1
    score_list = list()
    # heapify(score_list)
    saint_count = 0
    while saint_count < total_count:
        if saint_count % 10000 == 0:
            print("Processing count = %d" % saint_count)
        my_saint = Saint.generate_saint(pos)
        for i in range(5):
            my_saint.strength()
        score, _ = my_saint.score()
        # if saint_count >= heap_count:
        #     heappushpop(score_list, score)
        # else:
        #     heappush(score_list, score)
        score_list.append(score)
        saint_count += 1
        if saint_count % 1000000 == 0:
            score_list.sort()
            concern_ratio = range(0, 90)
            for i in range(90):
                concern_ratio.append(90 + i * 0.1)
            for i in range(90):
                concern_ratio.append(99 + i * 0.01)
            for i in range(90):
                concern_ratio.append(99.9 + i * 0.001)
            for i in range(90):
                concern_ratio.append(99.99 + i * 0.0001)
            for i in range(100):
                concern_ratio.append(99.999 + i * 0.00001)
            with open('ratio-%d.txt' % pos, 'w') as f:
                for i in concern_ratio:
                    # r = 100 * (saint_count - heap_count) / float(saint_count) + heap_count * i / float(saint_count)
                    r = i
                    print("%s%.5f分位得分：%.2f" % (position_name[pos], r, score_list[int(i * saint_count / 100)]))
                    f.write("%.5f\t%.2f\n" % (r, score_list[int(i * saint_count / 100)]))
