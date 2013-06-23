"""
The main execution entry-point for the webapp/services.
"""

# Import system stuff
from flask import Flask, request, session, g, redirect, url_for,\
    abort, render_template, flash, jsonify

from server import app


@app.before_request
def before_request_callback():
    pass


@app.teardown_request
def teardown_request(exception):
    """Request teardown handlers, which are called when every request is completed"""
    pass


@app.after_request
def after_request_callback(response):
    return response


@app.route("/", methods=['GET'])
def home():
    return "hola world"


if __name__ == "__main__":
    app.run(debug=True)
