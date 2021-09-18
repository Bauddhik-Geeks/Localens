from flask import Flask, request, jsonify
from util.connection import conn
from util.connection import cur

app = Flask(__name__)


@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route('/insert_data', methods=['POST'])
def insert_data():
    values = request.get_json()
    if not values:
        response = {'message': 'No data found.'}
        return jsonify("notfound"), 400
    if 'image64' not in values:
        response = {'message': 'Some data is missing'}
        return jsonify("datamissing"), 400
    #img = b64decode(values['image64'])

    print(len(values['image64']))

    query = "insert into add_locality(name,country,description,locality,address,place_image,latitude,longitude) values('{name}','{country}','{description}','{locality}','{address}','{img}','{lat}','{log}');".format(
        name=values['name'],
        country=values['country'],
        description=values['description'],
        locality=values['locality'],
        address=values['address'],
        img=values['image64'],
        lat=values['latitude'],
        log=values['longitude'])

    with open('q.txt', 'w') as f:
        f.write(query)
   
    try:
        cur.execute(query)
        conn.commit()
        return jsonify("success"), 200
    except:
        return jsonify("fail"), 400





@app.route('/fetch_data', methods=['POST'])
def fetch_data():
    values = request.get_json()
    if not values:
        return jsonify("notfound"), 400
    
    query = "select name,country,description,locality,address,latitude,longitude,id from add_locality ;"

     
    with open('q.txt', 'w') as f:
        f.write(query)

    list_dict = []
    temp_dict = {}

    try:
      cur.execute(query)
      rec = cur.fetchall()
      

      for record in rec:
        temp_dict['name'] = record[0]
        temp_dict['country'] = record[1]
        temp_dict['description'] = record[2]
        temp_dict['locality'] = record[3]
        temp_dict['address'] = record[4]
        temp_dict['latitude'] = record[5]
        temp_dict['longitude'] = record[6]
        temp_dict['id'] = record[7]
        list_dict.append(temp_dict)
        temp_dict = {}
      return jsonify(list_dict), 200
    except:
      return "", 400

      

@app.route('/fetch_image', methods=['POST'])
def fetch_image():
    values = request.get_json()
    if not values:
        return jsonify("notfound"), 400
    
    query = "select place_image from add_locality where latitude = '{lat}' and longitude = '{log}';".format(lat = values['latitude'],log=values['longitude'])

    try:
      cur.execute(query)
      rec = cur.fetchone()
      return jsonify(rec[0]),200
    except:
      return "",400

    
    


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8080)
