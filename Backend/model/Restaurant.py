import pymysql

def get_food_list(x,y):
    food_list = []
    db = pymysql.connect(host='204.236.163.157',user='JOONSOO',db='test_db',password='2411',charset='utf8')
    curs=db.cursor()
    sql = "SELECT *, ST_DISTANCE_SPHERE(POINT("+str(x)+","+str(y)+"), location) AS dist FROM Restaurant ORDER BY dist Limit 2;"
    print(sql)
    curs.execute(sql)

    rows = curs.fetchall()
    for e in rows:
        temp = {'id':e[0],'img_source':e[2],'link':e[3]}
        food_list.append(temp)
    
    db.commit()
    db.close()
    return food_list
# SET @lon = 126.955869;
# SET @lat = 37.546037;
 
# SELECT *, ST_DISTANCE_SPHERE(POINT(126.955869, 37.546037), location) AS dist FROM place ORDER BY dist Limit 2;