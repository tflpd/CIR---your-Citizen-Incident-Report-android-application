from rest_framework import serializers
from myServer.models import IncidentReport

class IncidentReportSerializer(serializers.ModelSerializer):
    class Meta:
        model = IncidentReport
        fields = ('id', 'description')