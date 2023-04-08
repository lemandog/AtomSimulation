import subprocess
import sys

subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'flask'])
subprocess.check_call([sys.executable, '-m', 'pip', 'install', 'numba'])

from flask import Flask, jsonify, make_response
from numba import cuda

app = Flask(__name__)


@app.route('/data')
def getCUDA():
    device = cuda.get_current_device()
    return "Устройство CUDA {}".format(device.name.decode())

@app.route('/health')
def getHeatlh():
    return make_response(201)

if __name__ == '__main__':
    app.run(debug=True)