# application/create.py
# 서버 객체를 생성, 환경 변수를 세팅 해준다.
from flask import Flask


def create_app(mode='dev'):
    app = Flask(__name__)
    return app