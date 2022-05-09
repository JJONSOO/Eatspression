import pymysql

def get_food_list(x,y,distance,num):
    food_list = []
    db = pymysql.connect(host='54.183.227.153',user='JOONSOO',db='test_db',password='2411',charset='utf8')
    curs=db.cursor()
    sql = "SELECT *, ST_DISTANCE_SPHERE(POINT("+str(x)+","+str(y)+"), location)"+"AS dist FROM Restaurant WHERE ST_DISTANCE_SPHERE(POINT("+str(x)+","+str(y)+"),location)<"+str(distance)+" ORDER BY dist limit "+str(num)+";"
    print(sql)
    curs.execute(sql)

    rows = curs.fetchall()
    for e in rows:
        img_source = {'img_source':e[2]}
        food_list.append(img_source)
    
    db.commit()
    db.close()
    return food_list