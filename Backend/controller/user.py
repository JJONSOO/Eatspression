from flask import Blueprint
from flask import request
from flask_restx import Namespace,Resource
import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from model import Restaurant
import json

def truncate(num,n):
    temp = str(num)
    for x in range(len(temp)):
        if temp[x] == '.':
            try:
                return float(temp[:x+n+1])
            except:
                return float(temp)      
    return float(temp)

user_api = Namespace('user_api')
@user_api.route("/")
class User_location(Resource):
    def get(self):
        return "get"
    def post(self):

        point = request.json
        x=truncate(point['x'],7)
        y=truncate(point['y'],7)
        distance=point['dist']
        num=point['recommendNum']

        food_list=Restaurant.get_food_list(x,y,distance,num)

        data={}
        data['img_list']=(food_list)
        tmp=json.dumps(data)

        return tmp

