from flask import Blueprint
from flask import request
from flask_restx import Namespace,Resource
import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))
from model import Restaurant
import json
# from app import app

user_api = Namespace('user_api')
@user_api.route("/")
class User_location(Resource):
    def get(self):
        return "get"
    def post(self):
        # tmp = str(request.data)
        # tmp = tmp[1:]
        # print(tmp)
        # point=json.loads(tmp)
        point = request.json
        print(request)
        x=point['x']
        y=point['y']
        print(x)
        print(y)
        tmp=Restaurant.get_food_list(x,y)
        tmp=dict(zip(range(1, len(tmp) + 1), tmp))
        print(tmp)
        return json.dumps(tmp)