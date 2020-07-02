require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name          = package["name"]
  s.version       = package["version"]
  s.summary       = package["description"]

  s.homepage      = package["repository"]["url"]
  s.license       = 'MIT'
  s.author        = package["author"]
  s.source        = { :git => package["repository"]["url"], :tag => "v#{s.version}" }  

  s.requires_arc   = true
  s.swift_version  = '5.0'
  s.platform       = :ios, "13.0"
  
  s.subspec "core" do |ss|
    ss.source_files = "ios/Core/**/*.{swift,h,m}"
  end

  s.subspec "view" do |ss|
    ss.source_files = "ios/View/**/*.{swift,h,m}"

    ss.dependency 'react-native-totvs-camera/core'
  end

  s.subspec "vision" do |ss|
    ss.source_files = "ios/Vision/**/*.{swift,h,m}"

    ss.dependency 'react-native-totvs-camera/core'            
    ss.dependency 'Firebase/MLVision'
    ss.dependency 'Firebase/MLVisionFaceModel'
    ss.dependency 'Firebase/MLVisionBarcodeModel'
  end   

  s.subspec "react-view" do |ss|
    ss.source_files = "ios/ReactView/**/*.{swift,h,m}"

    ss.dependency 'react-native-totvs-camera/view'
  end  

  s.default_subspecs = "core", "view", "vision"

  s.dependency 'React'
end
