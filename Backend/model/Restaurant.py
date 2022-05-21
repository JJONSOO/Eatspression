import pymysql
import random
def get_food_list(x,y,distance,num):
    db = pymysql.connect(host='54.241.56.66',user='JOONSOO',db='test_db',password='2411',charset='utf8')
    curs=db.cursor()
    # sql = "SELECT *, ST_DISTANCE_SPHERE(POINT("+str(x)+","+str(y)+"), location)"+"AS dist FROM Restaurant WHERE ST_DISTANCE_SPHERE(POINT("+str(x)+","+str(y)+"),location)<"+str(distance)+" ORDER BY dist limit "+str(num)+";"
    sql = "SELECT picture FROM Restaurant WHERE ST_DISTANCE_SPHERE(POINT("+str(x)+","+str(y)+"),location)<"+str(distance)+";"
    print(sql)
    curs.execute(sql)

    rows = curs.fetchall()
    food_list = []
    for e in rows:
        # img_source = {'img_source':e[2]}
        # food_list.append(img_source)
        temp=e[0]
        food_list.append(temp)
    if(len(food_list)<num):
        food_list=random.sample(food_list,len(food_list))
    else:
        food_list=random.sample(food_list,num)
    db.commit()
    db.close()
    return food_list

def get_food_link(food_picture):
    db = pymysql.connect(host='54.241.56.66',user='JOONSOO',db='test_db',password='2411',charset='utf8')
    curs=db.cursor()
    sql = "SELECT link FROM Restaurant WHERE picture =\""+str(food_picture)+"\";"
    curs.execute(sql)

    rows = curs.fetchall()
    print(rows[0][0])
    return rows[0][0]
