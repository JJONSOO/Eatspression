from __init__ import create_app

from flask_restx import Api
from controller.user import user_api

app = create_app()
api = Api(app)
api.add_namespace(user_api,path='/restraunt')
@app.route('/test', methods=['GET', 'POST'])
def test():
    return "hell"
if __name__ == '__main__':
    app.run(host='0.0.0.0',port='8080')