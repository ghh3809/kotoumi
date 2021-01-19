import json

tag_dict = dict()

with open('data.txt', 'r') as f:
    line = f.readline().strip()
    data_json = json.loads(line)
    for tag in data_json['tags']:
        tag_id = tag['tag_id']

        search_keyword_list = tag['search_keyword']
        if search_keyword_list is not None:
            tag_dict[str(tag_id)] = '|'.join(search_keyword_list)

    index = 0
    for unit_tag in data_json['unit_tags']:
        if index == 0:
            index += 1
            continue
        if unit_tag[0] is not None and str(unit_tag[0]) in tag_dict:
            tags = tag_dict[str(unit_tag[0])].replace('\'', '\\\'')
            if unit_tag[2] is not None:
                for tag in unit_tag[2]:
                    tags += '|' + tag.replace('\'', '\\\'')
            print("INSERT INTO kotoumi_tags VALUES (%d, '%s');" % (index, tags))
        index += 1

