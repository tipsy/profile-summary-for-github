from django.shortcuts import render
from django.http import HttpResponse

# Create your views here.

def home(request):
    """Trang chủ đơn giản"""
    return render(request, 'myapp/home.html')

def about(request):
    """Trang giới thiệu"""
    return render(request, 'myapp/about.html')

def hello(request):
    """View đơn giản trả về text"""
    return HttpResponse("Xin chào! Đây là ứng dụng Django đơn giản.")
