# locustfile.py
from locust import HttpUser, task

class WebsiteUser(HttpUser):
    min_wait = 5000
    max_wait = 9000

    @task(1)
    def index(self):
        self.client.get('/')

    @task(2)
    def all(self):
        self.client.get('/students')

    @task(2)
    def get_one(self):
        self.client.get('/student/10001')